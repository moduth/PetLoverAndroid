package com.morecruit.ext.component.logger;

class LogToFile extends LoggerFile.Log4jWrapper {

    private org.apache.log4j.Logger log4j = null;

    public LogToFile(org.apache.log4j.Logger log4j) {
        this.log4j = log4j;
    }

    public void trace(Object message) {
        try {
            log4j.trace(message);
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    public void trace(Object message, Throwable t) {
        try {
            log4j.trace(message, t);
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    public void debug(Object message) {
        try {
            log4j.debug(message);
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    public void debug(Object message, Throwable t) {
        try {
            log4j.debug(message, t);
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    public void info(Object message) {
        try {
            log4j.info(message);
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    public void info(Object message, Throwable t) {
        try {
            log4j.info(message, t);
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    public void warn(Object message) {
        try {
            log4j.warn(message);
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    public void warn(Object message, Throwable t) {
        try {
            log4j.warn(message, t);
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    public void warn(Throwable t) {
        try {
            log4j.warn(t, t);
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    public void error(Object message) {
        try {
            log4j.error(message);
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    public void error(Object message, Throwable t) {
        try {
            log4j.error(message, t);
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    public void error(Throwable t) {
        try {
            log4j.error(t, t);
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    public void fatal(Object message) {
        try {
            log4j.fatal(message);
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    public void fatal(Object message, Throwable t) {
        try {
            log4j.fatal(message, t);
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    public void fatal(Throwable t) {
        try {
            log4j.fatal(t, t);
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }
}
