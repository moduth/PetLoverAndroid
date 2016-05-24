package com.morecruit.ext.component.injector.core;

/**
 * Dummy Class for System ClassLoader Injection, <b>NEVER delete it, pls. </b><br>
 * <br>
 * For some DexClassLoader/LexClassLoader may have a lazy-loading scheme upon OS 4.0- / AliyunOS, <br>
 * loading this class leads to initialize their fields immediately, and then continue our classloader injection. <br>
 * <br>
 * (1) <b>NEVER obfuscate</b> this class<br>
 * (2) keep it as an only class in "com.morecruit.ext.component.injector.core" package <br>
 * (3) build it into extra dex, load it in code.<br>
 *
 * @see {@link com.morecruit.ext.component.injector.SystemClassLoaderInjector}
 * <p>
 * Created by zhaiyifan on 2015/8/4.
 */
public class AntiLazyLoad {

}