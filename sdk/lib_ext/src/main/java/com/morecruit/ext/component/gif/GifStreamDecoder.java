package com.morecruit.ext.component.gif;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * gif流解码
 */
public class GifStreamDecoder {
    /**
     * File read status: No errors.
     */
    public static final int STATUS_OK = 0;
    /**
     * File read status: Error decoding file (may be partially decoded)
     */
    public static final int STATUS_FORMAT_ERROR = 1;
    /**
     * File read status: Unable to open source.
     */
    public static final int STATUS_OPEN_ERROR = 2;
    /**
     * max decoder pixel stack size
     */
    protected static final int MAX_STACK_SIZE = 4096;

    protected InputStream in;
    protected String path = null;

    protected int status;
    protected State state;
    protected GifFrame frame;

    public static class GifFrame {
        public GifFrame(Bitmap im, int del) {
            image = im;
            delay = del;
        }

        public Bitmap image;
        public int delay;
    }

    public GifStreamDecoder(String path) {
        this.path = path;
    }

//	/**
//	 * Gets display duration for specified frame.
//	 *
//	 * @param n
//	 * int index of frame
//	 * @return delay in milliseconds
//	 */
//	public int getDelay(int n) {
//		delay = -1;
//		if ((n >= 0) && (n < frameCount)) {
//			delay = frames.elementAt(n).delay;
//		}
//		return delay;
//	}

//	/**
//	 * Gets the number of frames read from file.
//	 *
//	 * @return frame count
//	 */
//	public int getFrameCount() {
//		return frameCount;
//	}

//	/**
//	 * Gets the first (or only) image read.
//	 *
//	 * @return BufferedBitmap containing first frame, or null if none.
//	 */
//	public Bitmap getBitmap() {
//		return getFrame(0);
//	}

    /**
     * Gets the "Netscape" iteration count, if any. A count of 0 means repeat indefinitiely.
     *
     * @return iteration count if one was specified, else 1.
     */
    public int getLoopCount() {
        return state != null ? state.loopCount : 0;
    }

    /**
     * Creates new frame image from current data (and previous frames as specified by their disposition codes).
     */
    protected void setPixels() {
        // expose destination image's pixels as int array
        int[] dest = new int[state.width * state.height];
        // fill in starting image contents based on last image's dispose code
        if (state.lastDispose > 0) {
            if (state.lastDispose == 3) {
                // use image before last
                int n = state.frameCount - 2;
                if (n > 0) {
                    state.lastBitmap = null;//getFrame(n - 1);
                } else {
                    state.lastBitmap = null;
                }
            }
            if (state.lastBitmap != null) {
                state.lastBitmap.getPixels(dest, 0, state.width, 0, 0, state.width, state.height);
                // copy pixels
                if (state.lastDispose == 2) {
                    // fill last image rect area with background color
                    int c = 0;
                    if (!state.transparency) {
                        c = state.lastBgColor;
                    }
                    for (int i = 0; i < state.lrh; i++) {
                        int n1 = (state.lry + i) * state.width + state.lrx;
                        int n2 = n1 + state.lrw;
                        for (int k = n1; k < n2; k++) {
                            dest[k] = c;
                        }
                    }
                }
            }
        }
        // copy each source line to the appropriate place in the destination
        int pass = 1;
        int inc = 8;
        int iline = 0;
        for (int i = 0; i < state.ih; i++) {
            int line = i;
            if (state.interlace) {
                if (iline >= state.ih) {
                    pass++;
                    switch (pass) {
                        case 2:
                            iline = 4;
                            break;
                        case 3:
                            iline = 2;
                            inc = 4;
                            break;
                        case 4:
                            iline = 1;
                            inc = 2;
                            break;
                        default:
                            break;
                    }
                }
                line = iline;
                iline += inc;
            }
            line += state.iy;
            if (line < state.height) {
                int k = line * state.width;
                int dx = k + state.ix; // start of line in dest
                int dlim = dx + state.iw; // end of dest line
                if ((k + state.width) < dlim) {
                    dlim = k + state.width; // past dest edge
                }
                int sx = i * state.iw; // start of line in source
                while (dx < dlim) {
                    // map color and insert in destination
                    int index = ((int) state.pixels[sx++]) & 0xff;
                    int c = state.act[index];
                    if (c != 0) {
                        dest[dx] = c;
                    }
                    dx++;
                }
            }
        }
        state.image = Bitmap.createBitmap(dest, state.width, state.height, Config.ARGB_4444);
    }

//	/**
//	 * Gets the image contents of frame n.
//	 *
//	 * @return BufferedBitmap representation of frame, or null if n is invalid.
//	 */
//	public Bitmap getFrame(int n) {
//		if (frameCount <= 0)
//			return null;
//		n = n % frameCount;
//		return ((GifFrame) frames.elementAt(n)).image;
//	}
//

    private boolean open() {
        init();

        try {
            in = new FileInputStream(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (in != null) {
            readHeader();
            /*if (!err()) {
                readContents();
                if (frameCount < 0) {
                    status = STATUS_FORMAT_ERROR;
                }
            }*/
            if (err()) {
                status = STATUS_OPEN_ERROR;
            }
        } else {
            status = STATUS_OPEN_ERROR;
        }
        return status == STATUS_OK;
    }

    public GifFrame next() {
        if (in == null) {
            open();
        }
        if (in == null) {
            return null;
        }
        // read GIF file content blocks
        boolean read = false;
        boolean done = false;
        while (!(read || done || err())) {
            int code = read();
            switch (code) {
                case 0x2C: // image separator
                    readBitmap();
                    read = true;
                    break;
                case 0x21: // extension
                    code = read();
                    switch (code) {
                        case 0xf9: // graphics control extension
                            readGraphicControlExt();
                            break;
                        case 0xff: // application extension
                            readBlock();
                            StringBuilder app = new StringBuilder();
                            // String app = "";
                            for (int i = 0; i < 11; i++) {
                                app.append((char) state.block[i]);
                            }
                            String appStr = app.toString();
                            if (appStr.equals("NETSCAPE2.0")) {
                                readNetscapeExt();
                            } else {
                                skip(); // don't care
                            }
                            break;
                        case 0xfe:// comment extension
                            skip();
                            break;
                        case 0x01:// plain text extension
                            skip();
                            break;
                        default: // uninteresting extension
                            skip();
                    }
                    break;
                case 0x3b: // terminator
                    done = true;
                    break;
                case 0x00: // bad byte, but keep going and see what happens break;
                default:
                    status = STATUS_FORMAT_ERROR;
            }
        }
        GifFrame currFrame = frame;
        if (done) {
            // close all resources to prepare for loop read.
            close();
        }
        return currFrame;
    }

    public int close() {
        if (in != null) {
            try {
                in.close();
                in = null;
            } catch (Exception e) {
                // empty.
            }
        }
        state = null;
        frame = null;
        return 0;
    }

    public int getCurrentFrame() {
        return state != null ? state.frameCount : 0;
    }

//	/**
//	 * Reads GIF image from stream
//	 *
//	 * @param is
//	 * containing GIF file.
//	 * @return read status code (0 = no errors)
//	 */
//	public int read(InputStream is) {
//		init();
//		if (is != null) {
//			in = is;
//			readHeader();
//			if (!err()) {
//				readContents();
//				if (frameCount < 0) {
//					status = STATUS_FORMAT_ERROR;
//				}
//			}
//		} else {
//			status = STATUS_OPEN_ERROR;
//		}
//		try {
//			if (is != null) {
//				is.close();
//			}
//		} catch (Exception e) {
//		}
//		return status;
//	}

    /**
     * Decodes LZW image data into pixel array. Adapted from John Cristy's BitmapMagick.
     */
    protected void decodeBitmapData() {
        int nullCode = -1;
        int npix = state.iw * state.ih;
        int available, clear, code_mask, code_size, end_of_information, in_code, old_code, bits, code, count, i, datum, data_size, first, top, bi, pi;
        if ((state.pixels == null) || (state.pixels.length < npix)) {
            state.pixels = new byte[npix]; // allocate new pixel array
        }
        if (state.prefix == null) {
            state.prefix = new short[MAX_STACK_SIZE];
        }
        if (state.suffix == null) {
            state.suffix = new byte[MAX_STACK_SIZE];
        }
        if (state.pixelStack == null) {
            state.pixelStack = new byte[MAX_STACK_SIZE + 1];
        }
        // Initialize GIF data stream decoder.
        data_size = read();
        clear = 1 << data_size;
        end_of_information = clear + 1;
        available = clear + 2;
        old_code = nullCode;
        code_size = data_size + 1;
        code_mask = (1 << code_size) - 1;
        for (code = 0; code < clear; code++) {
            state.prefix[code] = 0; // XXX ArrayIndexOutOfBoundsException
            state.suffix[code] = (byte) code;
        }
        // Decode GIF pixel stream.
        datum = bits = count = first = top = pi = bi = 0;
        for (i = 0; i < npix; ) {
            if (top == 0) {
                if (bits < code_size) {
                    // Load bytes until there are enough bits for a code.
                    if (count == 0) {
                        // Read a new data block.
                        count = readBlock();
                        if (count <= 0) {
                            break;
                        }
                        bi = 0;
                    }
                    datum += (((int) state.block[bi]) & 0xff) << bits;
                    bits += 8;
                    bi++;
                    count--;
                    continue;
                }
                // Get the next code.
                code = datum & code_mask;
                datum >>= code_size;
                bits -= code_size;
                // Interpret the code
                if ((code > available) || (code == end_of_information)) {
                    break;
                }
                if (code == clear) {
                    // Reset decoder.
                    code_size = data_size + 1;
                    code_mask = (1 << code_size) - 1;
                    available = clear + 2;
                    old_code = nullCode;
                    continue;
                }
                if (old_code == nullCode) {
                    state.pixelStack[top++] = state.suffix[code];
                    old_code = code;
                    first = code;
                    continue;
                }
                in_code = code;
                if (code == available) {
                    state.pixelStack[top++] = (byte) first;
                    code = old_code;
                }
                while (code > clear) {
                    state.pixelStack[top++] = state.suffix[code];
                    code = state.prefix[code];
                }
                first = ((int) state.suffix[code]) & 0xff;
                // Add a new string to the string table,
                if (available >= MAX_STACK_SIZE) {
                    break;
                }
                state.pixelStack[top++] = (byte) first;
                state.prefix[available] = (short) old_code;
                state.suffix[available] = (byte) first;
                available++;
                if (((available & code_mask) == 0) && (available < MAX_STACK_SIZE)) {
                    code_size++;
                    code_mask += available;
                }
                old_code = in_code;
            }
            // Pop a pixel off the pixel stack.
            top--;
            state.pixels[pi++] = state.pixelStack[top];
            i++;
        }
        for (i = pi; i < npix; i++) {
            state.pixels[i] = 0; // clear missing pixels
        }
    }

    /**
     * Returns true if an error was encountered during reading/decoding
     */
    protected boolean err() {
        return status != STATUS_OK;
    }

    /**
     * Initializes or re-initializes reader
     */
    protected void init() {
        status = STATUS_OK;
        state = new State();
        state.frameCount = 0;
        //frames = new Vector<GifFrame>();
        state.gct = null;
        state.lct = null;
    }

    /**
     * Reads a single byte from the input stream.
     */
    protected int read() {
        int curByte = 0;
        try {
            curByte = in.read();
        } catch (Exception e) {
            status = STATUS_FORMAT_ERROR;
        }
        return curByte;
    }

    /**
     * Reads next variable length block from input.
     *
     * @return number of bytes stored in "buffer"
     */
    protected int readBlock() {
        state.blockSize = read();
        int n = 0;
        if (state.blockSize > 0) {
            try {
                int count = 0;
                while (n < state.blockSize) {
                    count = in.read(state.block, n, state.blockSize - n);
                    if (count == -1) {
                        break;
                    }
                    n += count;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (n < state.blockSize) {
                status = STATUS_FORMAT_ERROR;
            }
        }
        return n;
    }

    /**
     * Reads color table as 256 RGB integer values
     *
     * @param ncolors int number of colors to read
     * @return int array containing 256 colors (packed ARGB with full alpha)
     */
    protected int[] readColorTable(int ncolors) {
        int nbytes = 3 * ncolors;
        int[] tab = null;
        byte[] c = new byte[nbytes];
        int n = 0;
        try {
            n = in.read(c);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (n < nbytes) {
            status = STATUS_FORMAT_ERROR;
        } else {
            tab = new int[256]; // max size to avoid bounds checks
            int i = 0;
            int j = 0;
            while (i < ncolors) {
                int r = ((int) c[j++]) & 0xff;
                int g = ((int) c[j++]) & 0xff;
                int b = ((int) c[j++]) & 0xff;
                tab[i++] = 0xff000000 | (r << 16) | (g << 8) | b;
            }
        }
        return tab;
    }

    /**
     * Main file parser. Reads GIF content blocks.
     */
    protected void readContents() {
        // read GIF file content blocks
        boolean done = false;
        while (!(done || err())) {
            int code = read();
            switch (code) {
                case 0x2C: // image separator
                    readBitmap();
                    break;
                case 0x21: // extension
                    code = read();
                    switch (code) {
                        case 0xf9: // graphics control extension
                            readGraphicControlExt();
                            break;
                        case 0xff: // application extension
                            readBlock();
                            StringBuilder app = new StringBuilder();
                            // String app = "";
                            for (int i = 0; i < 11; i++) {
                                app.append((char) state.block[i]);
                            }
                            String appStr = app.toString();
                            if (appStr.equals("NETSCAPE2.0")) {
                                readNetscapeExt();
                            } else {
                                skip(); // don't care
                            }
                            break;
                        case 0xfe:// comment extension
                            skip();
                            break;
                        case 0x01:// plain text extension
                            skip();
                            break;
                        default: // uninteresting extension
                            skip();
                    }
                    break;
                case 0x3b: // terminator
                    done = true;
                    break;
                case 0x00: // bad byte, but keep going and see what happens break;
                default:
                    status = STATUS_FORMAT_ERROR;
            }
        }
    }

    /**
     * Reads Graphics Control Extension values
     */
    protected void readGraphicControlExt() {
        read(); // block size
        int packed = read(); // packed fields
        state.dispose = (packed & 0x1c) >> 2; // disposal method
        if (state.dispose == 0) {
            state.dispose = 1; // elect to keep old image if discretionary
        }
        state.transparency = (packed & 1) != 0;
        state.delay = readShort() * 10; // delay in milliseconds
        state.transIndex = read(); // transparent color index
        read(); // block terminator
    }

    /**
     * Reads GIF file header information.
     */
    protected void readHeader() {
        // String id = "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append((char) read());
        }
        String id = sb.toString();
        if (!id.startsWith("GIF")) {
            status = STATUS_FORMAT_ERROR;
            return;
        }
        readLSD();
        if (state.gctFlag && !err()) {
            state.gct = readColorTable(state.gctSize);
            state.bgColor = state.gct[state.bgIndex];
        }
    }

    /**
     * Reads next frame image
     */
    protected void readBitmap() {
        state.ix = readShort(); // (sub)image position & size
        state.iy = readShort();
        state.iw = readShort();
        state.ih = readShort();
        int packed = read();
        state.lctFlag = (packed & 0x80) != 0; // 1 - local color table flag interlace
        state.lctSize = (int) Math.pow(2, (packed & 0x07) + 1);
        // 3 - sort flag
        // 4-5 - reserved lctSize = 2 << (packed & 7); // 6-8 - local color
        // table size
        state.interlace = (packed & 0x40) != 0;
        if (state.lctFlag) {
            state.lct = readColorTable(state.lctSize); // read table
            state.act = state.lct; // make local table active
        } else {
            state.act = state.gct; // make global table active
            if (state.bgIndex == state.transIndex) {
                state.bgColor = 0;
            }
        }
        int save = 0;
        if (state.transparency) {
            save = state.act[state.transIndex];
            state.act[state.transIndex] = 0; // set transparent color if specified
        }
        if (state.act == null) {
            status = STATUS_FORMAT_ERROR; // no color table defined
        }
        if (err()) {
            return;
        }
        decodeBitmapData(); // decode pixel data
        skip();
        if (err()) {
            return;
        }
        state.frameCount++;
        // create new image to receive frame data
        state.image = Bitmap.createBitmap(state.width, state.height, Config.ARGB_4444);

        setPixels(); // transfer pixel data to image
        frame = new GifFrame(state.image, state.delay);
        // list
        if (state.transparency) {
            state.act[state.transIndex] = save;
        }
        resetFrame();
    }

    /**
     * Reads Logical Screen Descriptor
     */
    protected void readLSD() {
        // logical screen size
        state.width = readShort();
        state.height = readShort();
        // packed fields
        int packed = read();
        state.gctFlag = (packed & 0x80) != 0; // 1 : global color table flag
        // 2-4 : color resolution
        // 5 : gct sort flag
        state.gctSize = 2 << (packed & 7); // 6-8 : gct size
        state.bgIndex = read(); // background color index
        state.pixelAspect = read(); // pixel aspect ratio
    }

    /**
     * Reads Netscape extenstion to obtain iteration count
     */
    protected void readNetscapeExt() {
        do {
            readBlock();
            if (state.block[0] == 1) {
                // loop count sub-block
                int b1 = ((int) state.block[1]) & 0xff;
                int b2 = ((int) state.block[2]) & 0xff;
                state.loopCount = (b2 << 8) | b1;
            }
        } while ((state.blockSize > 0) && !err());
    }

    /**
     * Reads next 16-bit value, LSB first
     */
    protected int readShort() {
        // read 16-bit value, LSB first
        return read() | (read() << 8);
    }

    /**
     * Resets frame state for reading next image.
     */
    protected void resetFrame() {
        state.lastDispose = state.dispose;
        state.lrx = state.ix;
        state.lry = state.iy;
        state.lrw = state.iw;
        state.lrh = state.ih;
        state.lastBitmap = state.image;
        state.lastBgColor = state.bgColor;
        state.dispose = 0;
        state.transparency = false;
        state.delay = 0;
        state.lct = null;
    }

    /**
     * Skips variable length blocks up to and including next zero length block.
     */
    protected void skip() {
        do {
            readBlock();
        } while ((state.blockSize > 0) && !err());
    }

    protected final static class State {
        protected int width; // full image width
        protected int height; // full image height
        protected boolean gctFlag; // global color table used
        protected int gctSize; // size of global color table
        protected int loopCount = 1; // iterations; 0 = repeat forever
        protected int[] gct; // global color table
        protected int[] lct; // local color table
        protected int[] act; // active color table
        protected int bgIndex; // background color index
        protected int bgColor; // background color
        protected int lastBgColor; // previous bg color
        protected int pixelAspect; // pixel aspect ratio
        protected boolean lctFlag; // local color table flag
        protected boolean interlace; // interlace flag
        protected int lctSize; // local color table size
        protected int ix, iy, iw, ih; // current image rectangle
        protected int lrx, lry, lrw, lrh;
        protected Bitmap image; // current frame
        protected Bitmap lastBitmap; // previous frame
        protected byte[] block = new byte[256]; // current data block
        protected int blockSize = 0; // block size last graphic control extension info
        protected int dispose = 0; // 0=no action; 1=leave in place; 2=restore to bg; 3=restore to prev
        protected int lastDispose = 0;
        protected boolean transparency = false; // use transparent color
        protected int delay = 0; // delay in milliseconds
        protected int transIndex; // transparent color index
        // LZW decoder working arrays
        protected short[] prefix;
        protected byte[] suffix;
        protected byte[] pixelStack;
        protected byte[] pixels;
        protected int frameCount;
    }
}
