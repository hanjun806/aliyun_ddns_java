package com.vniapp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.security.AccessController;
import java.security.GeneralSecurityException;
import java.security.PrivilegedAction;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.GZIPInputStream;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HttpRequest {

	public static final String CHARSET_UTF8 = "UTF-8";
	public static final String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";
	public static final String CONTENT_TYPE_JSON = "application/json";
	public static final String ENCODING_GZIP = "gzip";
	public static final String HEADER_ACCEPT = "Accept";
	public static final String HEADER_ACCEPT_CHARSET = "Accept-Charset";
	public static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
	public static final String HEADER_AUTHORIZATION = "Authorization";
	public static final String HEADER_CACHE_CONTROL = "Cache-Control";
	public static final String HEADER_CONTENT_ENCODING = "Content-Encoding";
	public static final String HEADER_CONTENT_LENGTH = "Content-Length";
	public static final String HEADER_CONTENT_TYPE = "Content-Type";
	public static final String HEADER_DATE = "Date";
	public static final String HEADER_ETAG = "ETag";
	public static final String HEADER_EXPIRES = "Expires";
	public static final String HEADER_IF_NONE_MATCH = "If-None-Match";
	public static final String HEADER_LAST_MODIFIED = "Last-Modified";
	public static final String HEADER_LOCATION = "Location";
	public static final String HEADER_PROXY_AUTHORIZATION = "Proxy-Authorization";
	public static final String HEADER_REFERER = "Referer";
	public static final String HEADER_SERVER = "Server";
	public static final String HEADER_USER_AGENT = "User-Agent";
	public static final String METHOD_DELETE = "DELETE";
	public static final String METHOD_GET = "GET";
	public static final String METHOD_HEAD = "HEAD";
	public static final String METHOD_OPTIONS = "OPTIONS";
	public static final String METHOD_POST = "POST";
	public static final String METHOD_PUT = "PUT";
	public static final String METHOD_TRACE = "TRACE";
	public static final String PARAM_CHARSET = "charset";
	private static final String BOUNDARY = "00content0boundary00";
	private static final String CONTENT_TYPE_MULTIPART = "multipart/form-data; boundary=00content0boundary00";
	private static final String CRLF = "\r\n";
	private static final String[] EMPTY_STRINGS = new String[0];

	private static SSLSocketFactory TRUSTED_FACTORY;
	private static HostnameVerifier TRUSTED_VERIFIER;

	private static String getValidCharset(String charset) {
		if ((charset != null) && (charset.length() > 0)) {
			return charset;
		}
		return "UTF-8";
	}

	private static SSLSocketFactory getTrustedFactory() throws HttpRequest.HttpRequestException {
		/* 272 */ if (TRUSTED_FACTORY == null) {
			/* 273 */ TrustManager[] trustAllCerts = { new X509TrustManager() {
				public X509Certificate[] getAcceptedIssuers() {
					/* 276 */ return new X509Certificate[0];
				}

				public void checkClientTrusted(X509Certificate[] chain, String authType) {
				}

				public void checkServerTrusted(X509Certificate[] chain, String authType) {
				}
			} };

			try {
				/* 288 */ SSLContext context = SSLContext.getInstance("TLS");
				/* 289 */ context.init(null, trustAllCerts, new SecureRandom());
				/* 290 */ TRUSTED_FACTORY = context.getSocketFactory();
			} catch (GeneralSecurityException e) {
				/* 292 */ IOException ioException = new IOException(
						/* 293 */ "Security exception configuring SSL context");
				/* 294 */ ioException.initCause(e);
				/* 295 */ throw new HttpRequestException(ioException);
			}
		}

		/* 299 */ return TRUSTED_FACTORY;
	}

	private static HostnameVerifier getTrustedVerifier() {
		/* 303 */ if (TRUSTED_VERIFIER == null) {
			/* 304 */ TRUSTED_VERIFIER = new HostnameVerifier() {
				public boolean verify(String hostname, SSLSession session) {
					/* 307 */ return true;
				}
			};
		}
		/* 311 */ return TRUSTED_VERIFIER;
	}

	private static StringBuilder addPathSeparator(String baseUrl, StringBuilder result) {
		/* 320 */ if (baseUrl.indexOf(':') + 2 == baseUrl.lastIndexOf('/'))
			/* 321 */ result.append('/');
		/* 322 */ return result;
	}

	private static StringBuilder addParamPrefix(String baseUrl, StringBuilder result) {
		/* 328 */ int queryStart = baseUrl.indexOf('?');
		/* 329 */ int lastChar = result.length() - 1;
		/* 330 */ if (queryStart == -1) {
			/* 331 */ result.append('?');
			/* 332 */ } else if ((queryStart < lastChar) && (baseUrl.charAt(lastChar) != '&'))
			/* 333 */ result.append('&');
		/* 334 */ return result;
	}

	private static StringBuilder addParam(Object key, Object value, StringBuilder result) {
		/* 339 */ if ((value != null) && (value.getClass().isArray())) {
			/* 340 */ value = arrayToList(value);
		}
		/* 342 */ if ((value instanceof Iterable)) {
			/* 343 */ Iterator<?> iterator = ((Iterable) value).iterator();
			/* 344 */ while (iterator.hasNext()) {
				/* 345 */ result.append(key);
				/* 346 */ result.append("[]=");
				/* 347 */ Object element = iterator.next();
				/* 348 */ if (element != null)
					/* 349 */ result.append(element);
				/* 350 */ if (iterator.hasNext())
					/* 351 */ result.append("&");
			}
		} else {
			/* 354 */ result.append(key);
			/* 355 */ result.append("=");
			/* 356 */ if (value != null) {
				/* 357 */ result.append(value);
			}
		}
		/* 360 */ return result;
	}

	public static abstract interface ConnectionFactory {
		/* 387 */ public static final ConnectionFactory DEFAULT = new ConnectionFactory() {
			public HttpURLConnection create(URL url) throws IOException {
				/* 389 */ return (HttpURLConnection) url.openConnection();
			}

			/* 393 */ public HttpURLConnection create(URL url, Proxy proxy) throws IOException {
				return (HttpURLConnection) url.openConnection(proxy);
			}
		};

		public abstract HttpURLConnection create(URL paramURL) throws IOException;

		public abstract HttpURLConnection create(URL paramURL, Proxy paramProxy) throws IOException;
	}

	/* 398 */ private static ConnectionFactory CONNECTION_FACTORY = ConnectionFactory.DEFAULT;

	public static void setConnectionFactory(ConnectionFactory connectionFactory) {
		/* 404 */ if (connectionFactory == null) {
			/* 405 */ CONNECTION_FACTORY = ConnectionFactory.DEFAULT;
		} else {
			/* 407 */ CONNECTION_FACTORY = connectionFactory;
		}
	}

	public static abstract interface UploadProgress {
		/* 423 */ public static final UploadProgress DEFAULT = new UploadProgress() {
			public void onUpload(long uploaded, long total) {
			}
		};

		public abstract void onUpload(long paramLong1, long paramLong2);
	}

	public static class Base64 {
		private static final byte EQUALS_SIGN = 61;

		private static final String PREFERRED_ENCODING = "US-ASCII";

		/* 454 */ private static final byte[] _STANDARD_ALPHABET = { 65, 66, /* 455 */ 67, 68, 69, 70, 71, 72,
				/* 456 */ 73, 74, 75, 76, 77, 78, /* 457 */ 79, 80, 81, 82, 83, 84, /* 458 */ 85, 86, 87, 88, 89, 90,
				/* 459 */ 97, 98, 99, 100, 101, 102, /* 460 */ 103, 104, 105, 106, 107, 108, /* 461 */ 109, 110, 111,
				112, 113, 114, /* 462 */ 115, 116, 117, 118, 119, 120, /* 463 */ 121, 122, 48, 49, 50, 51, /* 464 */ 52,
				53, 54, 55, 56, 57, /* 465 */ 43, 47 };

		private static byte[] encode3to4(byte[] source, int srcOffset, int numSigBytes, byte[] destination,
				int destOffset) {
			/* 504 */ byte[] ALPHABET = _STANDARD_ALPHABET;

			/* 506 */ int inBuff = (numSigBytes > 0 ? source[srcOffset] << 24 >>> 8 : 0) |
			/* 507 */ (numSigBytes > 1 ? source[(srcOffset + 1)] << 24 >>> 16 : 0) |
			/* 508 */ (numSigBytes > 2 ? source[(srcOffset + 2)] << 24 >>> 24 : 0);

			/* 510 */ switch (numSigBytes) {
			case 3:
				/* 512 */ destination[destOffset] = ALPHABET[(inBuff >>> 18)];
				/* 513 */ destination[(destOffset + 1)] = ALPHABET[(inBuff >>> 12 & 0x3F)];
				/* 514 */ destination[(destOffset + 2)] = ALPHABET[(inBuff >>> 6 & 0x3F)];
				/* 515 */ destination[(destOffset + 3)] = ALPHABET[(inBuff & 0x3F)];
				/* 516 */ return destination;

			case 2:
				/* 519 */ destination[destOffset] = ALPHABET[(inBuff >>> 18)];
				/* 520 */ destination[(destOffset + 1)] = ALPHABET[(inBuff >>> 12 & 0x3F)];
				/* 521 */ destination[(destOffset + 2)] = ALPHABET[(inBuff >>> 6 & 0x3F)];
				/* 522 */ destination[(destOffset + 3)] = 61;
				/* 523 */ return destination;

			case 1:
				/* 526 */ destination[destOffset] = ALPHABET[(inBuff >>> 18)];
				/* 527 */ destination[(destOffset + 1)] = ALPHABET[(inBuff >>> 12 & 0x3F)];
				/* 528 */ destination[(destOffset + 2)] = 61;
				/* 529 */ destination[(destOffset + 3)] = 61;
				/* 530 */ return destination;
			}

			/* 533 */ return destination;
		}

		public static String encode(String string) {
			byte[] bytes;

			try {
				/* 546 */ bytes = string.getBytes("US-ASCII");
			} catch (UnsupportedEncodingException e) {
				byte[] bytes;
				/* 548 */ bytes = string.getBytes();
			}
			/* 550 */ return encodeBytes(bytes);
		}

		public static String encodeBytes(byte[] source) {
			/* 566 */ return encodeBytes(source, 0, source.length);
		}

		public static String encodeBytes(byte[] source, int off, int len) {
			/* 586 */ byte[] encoded = encodeBytesToBytes(source, off, len);
			try {
				/* 588 */ return new String(encoded, "US-ASCII");
			} catch (UnsupportedEncodingException uue) {
			}
			/* 590 */ return new String(encoded);
		}

		public static byte[] encodeBytesToBytes(byte[] source, int off, int len) {
			/* 615 */ if (source == null) {
				/* 616 */ throw new NullPointerException("Cannot serialize a null array.");
			}
			/* 618 */ if (off < 0) {
				/* 619 */ throw new IllegalArgumentException("Cannot have negative offset: " +
				/* 620 */ off);
			}
			/* 622 */ if (len < 0) {
				/* 623 */ throw new IllegalArgumentException("Cannot have length offset: " + len);
			}
			/* 625 */ if (off + len > source.length) {
				/* 626 */ throw new IllegalArgumentException(

						/* 628 */ String.format(
								/* 629 */ "Cannot have offset of %d and length of %d with array of length %d",
								new Object[] { /* 630 */ Integer.valueOf(off), Integer.valueOf(len),
										Integer.valueOf(source.length) }));
			}

			/* 633 */ int encLen = len / 3 * 4 + (len % 3 > 0 ? 4 : 0);

			/* 635 */ byte[] outBuff = new byte[encLen];

			/* 637 */ int d = 0;
			/* 638 */ int e = 0;
			/* 639 */ int len2 = len - 2;
			/* 640 */ for (; d < len2; e += 4) {
				/* 641 */ encode3to4(source, d + off, 3, outBuff, e);
				d += 3;
			}
			/* 643 */ if (d < len) {
				/* 644 */ encode3to4(source, d + off, len - d, outBuff, e);
				/* 645 */ e += 4;
			}

			/* 648 */ if (e <= outBuff.length - 1) {
				/* 649 */ byte[] finalOut = new byte[e];
				/* 650 */ System.arraycopy(outBuff, 0, finalOut, 0, e);
				/* 651 */ return finalOut;
			}
			/* 653 */ return outBuff;
		}
	}

	public static class HttpRequestException extends RuntimeException {
		private static final long serialVersionUID = -1170466989781746231L;

		public HttpRequestException(IOException cause) {
			/* 670 */ super();
		}

		public IOException getCause() {
			/* 680 */ return (IOException) super.getCause();
		}
	}

	protected static abstract class Operation<V> implements Callable<V> {
		protected abstract V run() throws HttpRequest.HttpRequestException, IOException;

		protected abstract void done() throws IOException;

		public V call() throws HttpRequest.HttpRequestException {
			/* 709 */ boolean thrown = false;
			try {
				/* 711 */ return (V) run();
			} catch (HttpRequest.HttpRequestException e) {
				/* 713 */ thrown = true;
				/* 714 */ throw e;
			} catch (IOException e) {
				/* 716 */ thrown = true;
				/* 717 */ throw new HttpRequest.HttpRequestException(e);
			} finally {
				try {
					/* 720 */ done();
				} catch (IOException e) {
					/* 722 */ if (!thrown) {
						/* 723 */ throw new HttpRequest.HttpRequestException(e);
					}
				}
			}
		}
	}

	protected static abstract class CloseOperation<V> extends HttpRequest.Operation<V> {
		private final Closeable closeable;

		private final boolean ignoreCloseExceptions;

		protected CloseOperation(Closeable closeable, boolean ignoreCloseExceptions) {
			/* 749 */ this.closeable = closeable;
			/* 750 */ this.ignoreCloseExceptions = ignoreCloseExceptions;
		}

		protected void done() throws IOException {
			/* 755 */ if ((this.closeable instanceof Flushable))
				/* 756 */ ((Flushable) this.closeable).flush();
			/* 757 */ if (this.ignoreCloseExceptions) {
				try {
					/* 759 */ this.closeable.close();

				} catch (IOException localIOException) {
				}
			} else {
				/* 764 */ this.closeable.close();
			}
		}
	}

	protected static abstract class FlushOperation<V> extends HttpRequest.Operation<V> {
		private final Flushable flushable;

		protected FlushOperation(Flushable flushable) {
			/* 784 */ this.flushable = flushable;
		}

		protected void done() throws IOException {
			/* 789 */ this.flushable.flush();
		}
	}

	public static class RequestOutputStream extends BufferedOutputStream {
		private final CharsetEncoder encoder;

		public RequestOutputStream(OutputStream stream, String charset, int bufferSize) {
			/* 809 */ super(bufferSize);

			/* 811 */ this.encoder = Charset.forName(HttpRequest.getValidCharset(charset)).newEncoder();
		}

		public RequestOutputStream write(String value) throws IOException {
			/* 822 */ ByteBuffer bytes = this.encoder.encode(CharBuffer.wrap(value));

			/* 824 */ super.write(bytes.array(), 0, bytes.limit());

			/* 826 */ return this;
		}
	}

	private static List<Object> arrayToList(Object array) {
		/* 836 */ if ((array instanceof Object[])) {
			/* 837 */ return Arrays.asList((Object[]) array);
		}
		/* 839 */ List<Object> result = new ArrayList();
		Object localObject1;
		/* 841 */ int j;
		int i;
		if ((array instanceof int[])) {
			/* 842 */ j = (localObject1 = (int[]) array).length;
			for (i = 0; i < j; i++) {
				int value = localObject1[i];
				result.add(Integer.valueOf(value));
				/* 843 */ }
		} else if ((array instanceof boolean[])) {
			/* 844 */ j = (localObject1 = (boolean[]) array).length;
			for (i = 0; i < j; i++) {
				boolean value = localObject1[i];
				result.add(Boolean.valueOf(value));
			}
		} else {
			Object localObject3;
			/* 845 */ if ((array instanceof long[])) {
				/* 846 */ int k = (localObject3 = (long[]) array).length;
				for (j = 0; j < k; j++) {
					long value = localObject3[j];
					result.add(Long.valueOf(value));
					/* 847 */ }
			} else if ((array instanceof float[])) {
				float[] arrayOfFloat;
				/* 848 */ j = (arrayOfFloat = (float[]) array).length;
				for (i = 0; i < j; i++) {
					float value = arrayOfFloat[i];
					result.add(Float.valueOf(value));
					/* 849 */ }
			} else if ((array instanceof double[])) {
				/* 850 */ int m = (localObject3 = (double[]) array).length;
				for (j = 0; j < m; j++) {
					double value = localObject3[j];
					result.add(Double.valueOf(value));
				}
			} else {
				Object localObject2;
				/* 851 */ if ((array instanceof short[])) {
					/* 852 */ j = (localObject2 = (short[]) array).length;
					for (i = 0; i < j; i++) {
						short value = localObject2[i];
						result.add(Short.valueOf(value));
						/* 853 */ }
				} else if ((array instanceof byte[])) {
					/* 854 */ j = (localObject2 = (byte[]) array).length;
					for (i = 0; i < j; i++) {
						byte value = localObject2[i];
						result.add(Byte.valueOf(value));
						/* 855 */ }
				} else if ((array instanceof char[])) {
					/* 856 */ j = (localObject2 = (char[]) array).length;
					for (i = 0; i < j; i++) {
						char value = localObject2[i];
						result.add(Character.valueOf(value));
					}
				}
			}
		}
		/* 857 */ return result;
	}

	public static String encode(CharSequence url) throws HttpRequest.HttpRequestException {
		try {
			/* 877 */ parsed = new URL(url.toString());
		} catch (IOException e) {
			URL parsed;
			/* 879 */ throw new HttpRequestException(e);
		}
		URL parsed;
		/* 882 */ String host = parsed.getHost();
		/* 883 */ int port = parsed.getPort();
		/* 884 */ if (port != -1) {
			/* 885 */ host = host + ':' + Integer.toString(port);
		}
		try {
			/* 888 */ String encoded = new URI(parsed.getProtocol(), host, parsed.getPath(),
					/* 889 */ parsed.getQuery(), null).toASCIIString();
			/* 890 */ int paramsStart = encoded.indexOf('?');
			/* 891 */ if ((paramsStart > 0) && (paramsStart + 1 < encoded.length())) {
			}
			/* 892 */ return
			/* 893 */ encoded.substring(0, paramsStart + 1) + encoded.substring(paramsStart + 1).replace("+", "%2B");
		} catch (URISyntaxException e) {
			/* 896 */ IOException io = new IOException("Parsing URI failed");
			/* 897 */ io.initCause(e);
			/* 898 */ throw new HttpRequestException(io);
		}
	}

	public static String append(CharSequence url, Map<?, ?> params) {
		/* 913 */ String baseUrl = url.toString();
		/* 914 */ if ((params == null) || (params.isEmpty())) {
			/* 915 */ return baseUrl;
		}
		/* 917 */ StringBuilder result = new StringBuilder(baseUrl);

		/* 919 */ addPathSeparator(baseUrl, result);
		/* 920 */ addParamPrefix(baseUrl, result);

		/* 923 */ Iterator<?> iterator = params.entrySet().iterator();
		/* 924 */ Map.Entry<?, ?> entry = (Map.Entry) iterator.next();
		/* 925 */ addParam(entry.getKey().toString(), entry.getValue(), result);

		/* 927 */ while (iterator.hasNext()) {
			/* 928 */ result.append('&');
			/* 929 */ entry = (Map.Entry) iterator.next();
			/* 930 */ addParam(entry.getKey().toString(), entry.getValue(), result);
		}

		/* 933 */ return result.toString();
	}

	public static String append(CharSequence url, Object... params) {
		/* 948 */ String baseUrl = url.toString();
		/* 949 */ if ((params == null) || (params.length == 0)) {
			/* 950 */ return baseUrl;
		}
		/* 952 */ if (params.length % 2 != 0) {
			/* 953 */ throw new IllegalArgumentException(
					/* 954 */ "Must specify an even number of parameter names/values");
		}
		/* 956 */ StringBuilder result = new StringBuilder(baseUrl);

		/* 958 */ addPathSeparator(baseUrl, result);
		/* 959 */ addParamPrefix(baseUrl, result);

		/* 961 */ addParam(params[0], params[1], result);

		/* 963 */ for (int i = 2; i < params.length; i += 2) {
			/* 964 */ result.append('&');
			/* 965 */ addParam(params[i], params[(i + 1)], result);
		}

		/* 968 */ return result.toString();
	}

	public static HttpRequest get(CharSequence url) throws HttpRequest.HttpRequestException {
		/* 980 */ return new HttpRequest(url, "GET");
	}

	public static HttpRequest get(URL url) throws HttpRequest.HttpRequestException {
		/* 991 */ return new HttpRequest(url, "GET");
	}

	public static HttpRequest get(CharSequence baseUrl, Map<?, ?> params, boolean encode) {
		/* 1010 */ String url = append(baseUrl, params);
		/* 1011 */ return get(encode ? encode(url) : url);
	}

	public static HttpRequest get(CharSequence baseUrl, boolean encode, Object... params) {
		/* 1031 */ String url = append(baseUrl, params);
		/* 1032 */ return get(encode ? encode(url) : url);
	}

	public static HttpRequest post(CharSequence url) throws HttpRequest.HttpRequestException {
		/* 1044 */ return new HttpRequest(url, "POST");
	}

	public static HttpRequest post(URL url) throws HttpRequest.HttpRequestException {
		/* 1055 */ return new HttpRequest(url, "POST");
	}

	public static HttpRequest post(CharSequence baseUrl, Map<?, ?> params, boolean encode) {
		/* 1074 */ String url = append(baseUrl, params);
		/* 1075 */ return post(encode ? encode(url) : url);
	}

	public static HttpRequest post(CharSequence baseUrl, boolean encode, Object... params) {
		/* 1095 */ String url = append(baseUrl, params);
		/* 1096 */ return post(encode ? encode(url) : url);
	}

	public static HttpRequest put(CharSequence url) throws HttpRequest.HttpRequestException {
		/* 1108 */ return new HttpRequest(url, "PUT");
	}

	public static HttpRequest put(URL url) throws HttpRequest.HttpRequestException {
		/* 1119 */ return new HttpRequest(url, "PUT");
	}

	public static HttpRequest put(CharSequence baseUrl, Map<?, ?> params, boolean encode) {
		/* 1138 */ String url = append(baseUrl, params);
		/* 1139 */ return put(encode ? encode(url) : url);
	}

	public static HttpRequest put(CharSequence baseUrl, boolean encode, Object... params) {
		/* 1159 */ String url = append(baseUrl, params);
		/* 1160 */ return put(encode ? encode(url) : url);
	}

	public static HttpRequest delete(CharSequence url) throws HttpRequest.HttpRequestException {
		/* 1172 */ return new HttpRequest(url, "DELETE");
	}

	public static HttpRequest delete(URL url) throws HttpRequest.HttpRequestException {
		/* 1183 */ return new HttpRequest(url, "DELETE");
	}

	public static HttpRequest delete(CharSequence baseUrl, Map<?, ?> params, boolean encode) {
		/* 1202 */ String url = append(baseUrl, params);
		/* 1203 */ return delete(encode ? encode(url) : url);
	}

	public static HttpRequest delete(CharSequence baseUrl, boolean encode, Object... params) {
		/* 1223 */ String url = append(baseUrl, params);
		/* 1224 */ return delete(encode ? encode(url) : url);
	}

	public static HttpRequest head(CharSequence url) throws HttpRequest.HttpRequestException {
		/* 1236 */ return new HttpRequest(url, "HEAD");
	}

	public static HttpRequest head(URL url) throws HttpRequest.HttpRequestException {
		/* 1247 */ return new HttpRequest(url, "HEAD");
	}

	public static HttpRequest head(CharSequence baseUrl, Map<?, ?> params, boolean encode) {
		/* 1266 */ String url = append(baseUrl, params);
		/* 1267 */ return head(encode ? encode(url) : url);
	}

	public static HttpRequest head(CharSequence baseUrl, boolean encode, Object... params) {
		/* 1287 */ String url = append(baseUrl, params);
		/* 1288 */ return head(encode ? encode(url) : url);
	}

	public static HttpRequest options(CharSequence url) throws HttpRequest.HttpRequestException {
		/* 1300 */ return new HttpRequest(url, "OPTIONS");
	}

	public static HttpRequest options(URL url) throws HttpRequest.HttpRequestException {
		/* 1311 */ return new HttpRequest(url, "OPTIONS");
	}

	public static HttpRequest trace(CharSequence url) throws HttpRequest.HttpRequestException {
		/* 1323 */ return new HttpRequest(url, "TRACE");
	}

	public static HttpRequest trace(URL url) throws HttpRequest.HttpRequestException {
		/* 1334 */ return new HttpRequest(url, "TRACE");
	}

	public static void keepAlive(boolean keepAlive) {
		/* 1345 */ setProperty("http.keepAlive", Boolean.toString(keepAlive));
	}

	public static void maxConnections(int maxConnections) {
		/* 1356 */ setProperty("http.maxConnections", Integer.toString(maxConnections));
	}

	public static void proxyHost(String host) {
		/* 1368 */ setProperty("http.proxyHost", host);
		/* 1369 */ setProperty("https.proxyHost", host);
	}

	public static void proxyPort(int port) {
		/* 1381 */ String portValue = Integer.toString(port);
		/* 1382 */ setProperty("http.proxyPort", portValue);
		/* 1383 */ setProperty("https.proxyPort", portValue);
	}

	public static void nonProxyHosts(String... hosts) {
		/* 1396 */ if ((hosts != null) && (hosts.length > 0)) {
			/* 1397 */ StringBuilder separated = new StringBuilder();
			/* 1398 */ int last = hosts.length - 1;
			/* 1399 */ for (int i = 0; i < last; i++)
				/* 1400 */ separated.append(hosts[i]).append('|');
			/* 1401 */ separated.append(hosts[last]);
			/* 1402 */ setProperty("http.nonProxyHosts", separated.toString());
		} else {
			/* 1404 */ setProperty("http.nonProxyHosts", null);
		}
	}

	private static String setProperty(String name, final String value) {
		PrivilegedAction<String> action;

		PrivilegedAction<String> action;

		/* 1418 */ if (value != null) {
			/* 1419 */ action = new PrivilegedAction() {
				public String run() {
					/* 1422 */ return System.setProperty(HttpRequest.this, value);
				}
			};
		} else
			/* 1426 */ action = new PrivilegedAction() {
				public String run() {
					/* 1429 */ return System.clearProperty(HttpRequest.this);
				}
			};
		/* 1432 */ return (String) AccessController.doPrivileged(action);
	}

	/* 1435 */ private HttpURLConnection connection = null;

	private final URL url;

	private final String requestMethod;

	private RequestOutputStream output;

	private boolean multipart;

	private boolean form;

	/* 1447 */ private boolean ignoreCloseExceptions = true;

	/* 1449 */ private boolean uncompress = false;

	/* 1451 */ private int bufferSize = 8192;

	/* 1453 */ private long totalSize = -1L;

	/* 1455 */ private long totalWritten = 0L;

	private String httpProxyHost;

	private int httpProxyPort;

	/* 1461 */ private UploadProgress progress = UploadProgress.DEFAULT;

	public HttpRequest(CharSequence url, String method) throws HttpRequest.HttpRequestException {
		try {
			/* 1473 */ this.url = new URL(url.toString());
		} catch (MalformedURLException e) {
			/* 1475 */ throw new HttpRequestException(e);
		}
		/* 1477 */ this.requestMethod = method;
	}

	public HttpRequest(URL url, String method) throws HttpRequest.HttpRequestException {
		/* 1489 */ this.url = url;
		/* 1490 */ this.requestMethod = method;
	}

	/* 1494 */ private Proxy createProxy() {
		return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(this.httpProxyHost, this.httpProxyPort));
	}

	private HttpURLConnection createConnection() {
		try {
			HttpURLConnection connection;
			HttpURLConnection connection;
			/* 1500 */ if (this.httpProxyHost != null) {
				/* 1501 */ connection = CONNECTION_FACTORY.create(this.url, createProxy());
			} else
				/* 1503 */ connection = CONNECTION_FACTORY.create(this.url);
			/* 1504 */ connection.setRequestMethod(this.requestMethod);
			/* 1505 */ return connection;
		} catch (IOException e) {
			/* 1507 */ throw new HttpRequestException(e);
		}
	}

	public String toString() {
		/* 1513 */ return method() + ' ' + url();
	}

	public HttpURLConnection getConnection() {
		/* 1522 */ if (this.connection == null)
			/* 1523 */ this.connection = createConnection();
		/* 1524 */ return this.connection;
	}

	public HttpRequest ignoreCloseExceptions(boolean ignore) {
		/* 1537 */ this.ignoreCloseExceptions = ignore;
		/* 1538 */ return this;
	}

	public boolean ignoreCloseExceptions() {
		/* 1548 */ return this.ignoreCloseExceptions;
	}

	public int code() throws HttpRequest.HttpRequestException {
		try {
			/* 1559 */ closeOutput();
			/* 1560 */ return getConnection().getResponseCode();
		} catch (IOException e) {
			/* 1562 */ throw new HttpRequestException(e);
		}
	}

	public HttpRequest code(AtomicInteger output) throws HttpRequest.HttpRequestException {
		/* 1576 */ output.set(code());
		/* 1577 */ return this;
	}

	public boolean ok() throws HttpRequest.HttpRequestException {
		/* 1587 */ return 200 == code();
	}

	public boolean created() throws HttpRequest.HttpRequestException {
		/* 1597 */ return 201 == code();
	}

	public boolean noContent() throws HttpRequest.HttpRequestException {
		/* 1607 */ return 204 == code();
	}

	public boolean serverError() throws HttpRequest.HttpRequestException {
		/* 1617 */ return 500 == code();
	}

	public boolean badRequest() throws HttpRequest.HttpRequestException {
		/* 1627 */ return 400 == code();
	}

	public boolean notFound() throws HttpRequest.HttpRequestException {
		/* 1637 */ return 404 == code();
	}

	public boolean notModified() throws HttpRequest.HttpRequestException {
		/* 1647 */ return 304 == code();
	}

	public String message() throws HttpRequest.HttpRequestException {
		try {
			/* 1658 */ closeOutput();
			/* 1659 */ return getConnection().getResponseMessage();
		} catch (IOException e) {
			/* 1661 */ throw new HttpRequestException(e);
		}
	}

	public HttpRequest disconnect() {
		/* 1671 */ getConnection().disconnect();
		/* 1672 */ return this;
	}

	public HttpRequest chunk(int size) {
		/* 1682 */ getConnection().setChunkedStreamingMode(size);
		/* 1683 */ return this;
	}

	public HttpRequest bufferSize(int size) {
		/* 1698 */ if (size < 1)
			/* 1699 */ throw new IllegalArgumentException("Size must be greater than zero");
		/* 1700 */ this.bufferSize = size;
		/* 1701 */ return this;
	}

	public int bufferSize() {
		/* 1712 */ return this.bufferSize;
	}

	public HttpRequest uncompress(boolean uncompress) {
		/* 1734 */ this.uncompress = uncompress;
		/* 1735 */ return this;
	}

	protected ByteArrayOutputStream byteStream() {
		/* 1744 */ int size = contentLength();
		/* 1745 */ if (size > 0) {
			/* 1746 */ return new ByteArrayOutputStream(size);
		}
		/* 1748 */ return new ByteArrayOutputStream();
	}

	public String body(String charset) throws HttpRequest.HttpRequestException {
		/* 1762 */ ByteArrayOutputStream output = byteStream();
		try {
			/* 1764 */ copy(buffer(), output);
			/* 1765 */ return output.toString(getValidCharset(charset));
		} catch (IOException e) {
			/* 1767 */ throw new HttpRequestException(e);
		}
	}

	public String body() throws HttpRequest.HttpRequestException {
		/* 1779 */ return body(charset());
	}

	public HttpRequest body(AtomicReference<String> output) throws HttpRequest.HttpRequestException {
		/* 1791 */ output.set(body());
		/* 1792 */ return this;
	}

	public HttpRequest body(AtomicReference<String> output, String charset) throws HttpRequest.HttpRequestException {
		/* 1805 */ output.set(body(charset));
		/* 1806 */ return this;
	}

	public boolean isBodyEmpty() throws HttpRequest.HttpRequestException {
		/* 1817 */ return contentLength() == 0;
	}

	public byte[] bytes() throws HttpRequest.HttpRequestException {
		/* 1827 */ ByteArrayOutputStream output = byteStream();
		try {
			/* 1829 */ copy(buffer(), output);
		} catch (IOException e) {
			/* 1831 */ throw new HttpRequestException(e);
		}
		/* 1833 */ return output.toByteArray();
	}

	public BufferedInputStream buffer() throws HttpRequest.HttpRequestException {
		/* 1844 */ return new BufferedInputStream(stream(), this.bufferSize);
	}

	public InputStream stream() throws HttpRequest.HttpRequestException {
		InputStream stream;

		/* 1855 */ if (code() < 400) {
			try {
				/* 1857 */ stream = getConnection().getInputStream();
			} catch (IOException e) {
				InputStream stream;
				/* 1859 */ throw new HttpRequestException(e);
			}
		} else {
			/* 1862 */ stream = getConnection().getErrorStream();
			/* 1863 */ if (stream == null) {
				try {
					/* 1865 */ stream = getConnection().getInputStream();
				} catch (IOException e) {
					/* 1867 */ if (contentLength() > 0) {
						/* 1868 */ throw new HttpRequestException(e);
					}
					/* 1870 */ stream = new ByteArrayInputStream(new byte[0]);
				}
			}
		}
		/* 1874 */ if ((!this.uncompress) || (!"gzip".equals(contentEncoding()))) {
			/* 1875 */ return stream;
		}
		try {
			/* 1878 */ return new GZIPInputStream(stream);
		} catch (IOException e) {
			/* 1880 */ throw new HttpRequestException(e);
		}
	}

	public InputStreamReader reader(String charset) throws HttpRequest.HttpRequestException {
		try {
			/* 1897 */ return new InputStreamReader(stream(), getValidCharset(charset));
		} catch (UnsupportedEncodingException e) {
			/* 1899 */ throw new HttpRequestException(e);
		}
	}

	public InputStreamReader reader() throws HttpRequest.HttpRequestException {
		/* 1911 */ return reader(charset());
	}

	public BufferedReader bufferedReader(String charset) throws HttpRequest.HttpRequestException {
		/* 1926 */ return new BufferedReader(reader(charset), this.bufferSize);
	}

	public BufferedReader bufferedReader() throws HttpRequest.HttpRequestException {
		/* 1938 */ return bufferedReader(charset());
	}

	public HttpRequest receive(File file)
     throws HttpRequest.HttpRequestException
   {
     try
     {
/* 1951 */       output = new BufferedOutputStream(new FileOutputStream(file), this.bufferSize);
     } catch (FileNotFoundException e) { OutputStream output;
/* 1953 */       throw new HttpRequestException(e); }
     final OutputStream output;
/* 1955 */     
     
 
 
 
 
/* 1961 */       (HttpRequest)new CloseOperation(output, this.ignoreCloseExceptions)
       {
         protected HttpRequest run()
           throws HttpRequest.HttpRequestException, IOException
         {
/* 1959 */           return HttpRequest.this.receive(output);
         }
       }.call();
   }

	public HttpRequest receive(OutputStream output) throws HttpRequest.HttpRequestException {
		try {
			/* 1974 */ return copy(buffer(), output);
		} catch (IOException e) {
			/* 1976 */ throw new HttpRequestException(e);
		}
	}

	public HttpRequest receive(PrintStream output) throws HttpRequest.HttpRequestException {
		/* 1989 */ return receive(output);
	}

	public HttpRequest receive(final Appendable appendable)
     throws HttpRequest.HttpRequestException
   {
/* 2001 */     final BufferedReader reader = bufferedReader();
     
 
 
 
 
 
 
 
 
 
 
 
 
/* 2015 */     (HttpRequest)new CloseOperation(reader, this.ignoreCloseExceptions)
     {
       public HttpRequest run() throws IOException
       {
/* 2006 */         CharBuffer buffer = CharBuffer.allocate(HttpRequest.this.bufferSize);
         int read;
/* 2008 */         while ((read = reader.read(buffer)) != -1) { int read;
/* 2009 */           buffer.rewind();
/* 2010 */           appendable.append(buffer, 0, read);
/* 2011 */           buffer.rewind();
         }
/* 2013 */         return HttpRequest.this;
       }
     }.call();
   }

	public HttpRequest receive(final Writer writer)
     throws HttpRequest.HttpRequestException
   {
/* 2026 */     final BufferedReader reader = bufferedReader();
     
 
 
 
 
 
/* 2033 */     (HttpRequest)new CloseOperation(reader, this.ignoreCloseExceptions)
     {
       public HttpRequest run() throws IOException
       {
/* 2031 */         return HttpRequest.this.copy(reader, writer);
       }
     }.call();
   }

	public HttpRequest readTimeout(int timeout) {
		/* 2043 */ getConnection().setReadTimeout(timeout);
		/* 2044 */ return this;
	}

	public HttpRequest connectTimeout(int timeout) {
		/* 2054 */ getConnection().setConnectTimeout(timeout);
		/* 2055 */ return this;
	}

	public HttpRequest header(String name, String value) {
		/* 2066 */ getConnection().setRequestProperty(name, value);
		/* 2067 */ return this;
	}

	public HttpRequest header(String name, Number value) {
		/* 2078 */ return header(name, value != null ? value.toString() : null);
	}

	public HttpRequest headers(Map<String, String> headers) {
		/* 2089 */ if (!headers.isEmpty())
			/* 2090 */ for (Map.Entry<String, String> header : headers.entrySet())
				/* 2091 */ header(header);
		/* 2092 */ return this;
	}

	public HttpRequest header(Map.Entry<String, String> header) {
		/* 2102 */ return header((String) header.getKey(), (String) header.getValue());
	}

	public String header(String name) throws HttpRequest.HttpRequestException {
		/* 2113 */ closeOutputQuietly();
		/* 2114 */ return getConnection().getHeaderField(name);
	}

	public Map<String, List<String>> headers() throws HttpRequest.HttpRequestException {
		/* 2124 */ closeOutputQuietly();
		/* 2125 */ return getConnection().getHeaderFields();
	}

	public long dateHeader(String name) throws HttpRequest.HttpRequestException {
		/* 2137 */ return dateHeader(name, -1L);
	}

	public long dateHeader(String name, long defaultValue) throws HttpRequest.HttpRequestException {
		/* 2151 */ closeOutputQuietly();
		/* 2152 */ return getConnection().getHeaderFieldDate(name, defaultValue);
	}

	public int intHeader(String name) throws HttpRequest.HttpRequestException {
		/* 2164 */ return intHeader(name, -1);
	}

	public int intHeader(String name, int defaultValue) throws HttpRequest.HttpRequestException {
		/* 2179 */ closeOutputQuietly();
		/* 2180 */ return getConnection().getHeaderFieldInt(name, defaultValue);
	}

	public String[] headers(String name) {
		/* 2190 */ Map<String, List<String>> headers = headers();
		/* 2191 */ if ((headers == null) || (headers.isEmpty())) {
			/* 2192 */ return EMPTY_STRINGS;
		}
		/* 2194 */ List<String> values = (List) headers.get(name);
		/* 2195 */ if ((values != null) && (!values.isEmpty())) {
			/* 2196 */ return (String[]) values.toArray(new String[values.size()]);
		}
		/* 2198 */ return EMPTY_STRINGS;
	}

	public String parameter(String headerName, String paramName) {
		/* 2209 */ return getParam(header(headerName), paramName);
	}

	public Map<String, String> parameters(String headerName) {
		/* 2222 */ return getParams(header(headerName));
	}

	protected Map<String, String> getParams(String header) {
		/* 2232 */ if ((header == null) || (header.length() == 0)) {
			/* 2233 */ return Collections.emptyMap();
		}
		/* 2235 */ int headerLength = header.length();
		/* 2236 */ int start = header.indexOf(';') + 1;
		/* 2237 */ if ((start == 0) || (start == headerLength)) {
			/* 2238 */ return Collections.emptyMap();
		}
		/* 2240 */ int end = header.indexOf(';', start);
		/* 2241 */ if (end == -1) {
			/* 2242 */ end = headerLength;
		}
		/* 2244 */ Map<String, String> params = new LinkedHashMap();
		/* 2245 */ while (start < end) {
			/* 2246 */ int nameEnd = header.indexOf('=', start);
			/* 2247 */ if ((nameEnd != -1) && (nameEnd < end)) {
				/* 2248 */ String name = header.substring(start, nameEnd).trim();
				/* 2249 */ if (name.length() > 0) {
					/* 2250 */ String value = header.substring(nameEnd + 1, end).trim();
					/* 2251 */ int length = value.length();
					/* 2252 */ if (length != 0) {
						/* 2253 */ if ((length > 2) && ('"' == value.charAt(0)) &&
						/* 2254 */ ('"' == value.charAt(length - 1))) {
							/* 2255 */ params.put(name, value.substring(1, length - 1));
						} else
							/* 2257 */ params.put(name, value);
					}
				}
			}
			/* 2261 */ start = end + 1;
			/* 2262 */ end = header.indexOf(';', start);
			/* 2263 */ if (end == -1) {
				/* 2264 */ end = headerLength;
			}
		}
		/* 2267 */ return params;
	}

	protected String getParam(String value, String paramName) {
		/* 2278 */ if ((value == null) || (value.length() == 0)) {
			/* 2279 */ return null;
		}
		/* 2281 */ int length = value.length();
		/* 2282 */ int start = value.indexOf(';') + 1;
		/* 2283 */ if ((start == 0) || (start == length)) {
			/* 2284 */ return null;
		}
		/* 2286 */ int end = value.indexOf(';', start);
		/* 2287 */ if (end == -1) {
			/* 2288 */ end = length;
		}
		/* 2290 */ while (start < end) {
			/* 2291 */ int nameEnd = value.indexOf('=', start);
			/* 2292 */ if ((nameEnd != -1) && (nameEnd < end) &&
			/* 2293 */ (paramName.equals(value.substring(start, nameEnd).trim()))) {
				/* 2294 */ String paramValue = value.substring(nameEnd + 1, end).trim();
				/* 2295 */ int valueLength = paramValue.length();
				/* 2296 */ if (valueLength != 0) {
					/* 2297 */ if ((valueLength > 2) && ('"' == paramValue.charAt(0)) &&
					/* 2298 */ ('"' == paramValue.charAt(valueLength - 1))) {
						/* 2299 */ return paramValue.substring(1, valueLength - 1);
					}
					/* 2301 */ return paramValue;
				}
			}
			/* 2304 */ start = end + 1;
			/* 2305 */ end = value.indexOf(';', start);
			/* 2306 */ if (end == -1) {
				/* 2307 */ end = length;
			}
		}
		/* 2310 */ return null;
	}

	public String charset() {
		/* 2319 */ return parameter("Content-Type", "charset");
	}

	public HttpRequest userAgent(String userAgent) {
		/* 2329 */ return header("User-Agent", userAgent);
	}

	public HttpRequest referer(String referer) {
		/* 2339 */ return header("Referer", referer);
	}

	public HttpRequest useCaches(boolean useCaches) {
		/* 2349 */ getConnection().setUseCaches(useCaches);
		/* 2350 */ return this;
	}

	public HttpRequest acceptEncoding(String acceptEncoding) {
		/* 2360 */ return header("Accept-Encoding", acceptEncoding);
	}

	public HttpRequest acceptGzipEncoding() {
		/* 2370 */ return acceptEncoding("gzip");
	}

	public HttpRequest acceptCharset(String acceptCharset) {
		/* 2380 */ return header("Accept-Charset", acceptCharset);
	}

	public String contentEncoding() {
		/* 2389 */ return header("Content-Encoding");
	}

	public String server() {
		/* 2398 */ return header("Server");
	}

	public long date() {
		/* 2407 */ return dateHeader("Date");
	}

	public String cacheControl() {
		/* 2416 */ return header("Cache-Control");
	}

	public String eTag() {
		/* 2425 */ return header("ETag");
	}

	public long expires() {
		/* 2434 */ return dateHeader("Expires");
	}

	public long lastModified() {
		/* 2443 */ return dateHeader("Last-Modified");
	}

	public String location() {
		/* 2452 */ return header("Location");
	}

	public HttpRequest authorization(String authorization) {
		/* 2462 */ return header("Authorization", authorization);
	}

	public HttpRequest proxyAuthorization(String proxyAuthorization) {
		/* 2472 */ return header("Proxy-Authorization", proxyAuthorization);
	}

	public HttpRequest basic(String name, String password) {
		/* 2484 */ return authorization("Basic "
				+ Base64.encode(new StringBuilder(String.valueOf(name)).append(':').append(password).toString()));
	}

	public HttpRequest proxyBasic(String name, String password) {
		/* 2496 */ return proxyAuthorization("Basic "
				+ Base64.encode(new StringBuilder(String.valueOf(name)).append(':').append(password).toString()));
	}

	public HttpRequest ifModifiedSince(long ifModifiedSince) {
		/* 2506 */ getConnection().setIfModifiedSince(ifModifiedSince);
		/* 2507 */ return this;
	}

	public HttpRequest ifNoneMatch(String ifNoneMatch) {
		/* 2517 */ return header("If-None-Match", ifNoneMatch);
	}

	public HttpRequest contentType(String contentType) {
		/* 2527 */ return contentType(contentType, null);
	}

	public HttpRequest contentType(String contentType, String charset) {
		/* 2538 */ if ((charset != null) && (charset.length() > 0)) {
			/* 2539 */ String separator = "; charset=";
			/* 2540 */ return header("Content-Type", contentType + "; charset=" + charset);
		}
		/* 2542 */ return header("Content-Type", contentType);
	}

	public String contentType() {
		/* 2551 */ return header("Content-Type");
	}

	public int contentLength() {
		/* 2560 */ return intHeader("Content-Length");
	}

	public HttpRequest contentLength(String contentLength) {
		/* 2570 */ return contentLength(Integer.parseInt(contentLength));
	}

	public HttpRequest contentLength(int contentLength) {
		/* 2580 */ getConnection().setFixedLengthStreamingMode(contentLength);
		/* 2581 */ return this;
	}

	public HttpRequest accept(String accept) {
		/* 2591 */ return header("Accept", accept);
	}

	public HttpRequest acceptJson() {
		/* 2600 */ return accept("application/json");
	}

	protected HttpRequest copy(final InputStream input, final OutputStream output)
     throws IOException
   {
/* 2613 */     
     
 
 
 
 
 
 
 
 
 
 
 
/* 2626 */       (HttpRequest)new CloseOperation(input, this.ignoreCloseExceptions)
       {
         public HttpRequest run()
           throws IOException
         {
/* 2617 */           byte[] buffer = new byte[HttpRequest.this.bufferSize];
           int read;
/* 2619 */           while ((read = input.read(buffer)) != -1) { int read;
/* 2620 */             output.write(buffer, 0, read);
/* 2621 */             HttpRequest.this.totalWritten += read;
/* 2622 */             HttpRequest.this.progress.onUpload(HttpRequest.this.totalWritten, HttpRequest.this.totalSize);
           }
/* 2624 */           return HttpRequest.this;
         }
       }.call();
   }

	protected HttpRequest copy(final Reader input, final Writer output)
     throws IOException
   {
/* 2639 */     
     
 
 
 
 
 
 
 
 
 
 
 
/* 2652 */       (HttpRequest)new CloseOperation(input, this.ignoreCloseExceptions)
       {
         public HttpRequest run()
           throws IOException
         {
/* 2643 */           char[] buffer = new char[HttpRequest.this.bufferSize];
           int read;
/* 2645 */           while ((read = input.read(buffer)) != -1) { int read;
/* 2646 */             output.write(buffer, 0, read);
/* 2647 */             HttpRequest.this.totalWritten += read;
/* 2648 */             HttpRequest.this.progress.onUpload(HttpRequest.this.totalWritten, -1L);
           }
/* 2650 */           return HttpRequest.this;
         }
       }.call();
   }

	public HttpRequest progress(UploadProgress callback) {
		/* 2662 */ if (callback == null) {
			/* 2663 */ this.progress = UploadProgress.DEFAULT;
		} else
			/* 2665 */ this.progress = callback;
		/* 2666 */ return this;
	}

	private HttpRequest incrementTotalSize(long size) {
		/* 2670 */ if (this.totalSize == -1L)
			/* 2671 */ this.totalSize = 0L;
		/* 2672 */ this.totalSize += size;
		/* 2673 */ return this;
	}

	protected HttpRequest closeOutput() throws IOException {
		/* 2684 */ progress(null);
		/* 2685 */ if (this.output == null)
			/* 2686 */ return this;
		/* 2687 */ if (this.multipart)
			/* 2688 */ this.output.write("\r\n--00content0boundary00--\r\n");
		/* 2689 */ if (this.ignoreCloseExceptions) {
			try {
				/* 2691 */ this.output.close();

			} catch (IOException localIOException) {
			}
		} else
			/* 2696 */ this.output.close();
		/* 2697 */ this.output = null;
		/* 2698 */ return this;
	}

	protected HttpRequest closeOutputQuietly() throws HttpRequest.HttpRequestException {
		try {
			/* 2710 */ return closeOutput();
		} catch (IOException e) {
			/* 2712 */ throw new HttpRequestException(e);
		}
	}

	protected HttpRequest openOutput() throws IOException {
		/* 2723 */ if (this.output != null)
			/* 2724 */ return this;
		/* 2725 */ getConnection().setDoOutput(true);
		/* 2726 */ String charset = getParam(/* 2727 */ getConnection().getRequestProperty("Content-Type"), "charset");
		/* 2728 */ this.output = new RequestOutputStream(getConnection().getOutputStream(), charset,
				/* 2729 */ this.bufferSize);
		/* 2730 */ return this;
	}

	protected HttpRequest startPart() throws IOException {
		/* 2740 */ if (!this.multipart) {
			/* 2741 */ this.multipart = true;
			/* 2742 */ contentType("multipart/form-data; boundary=00content0boundary00").openOutput();
			/* 2743 */ this.output.write("--00content0boundary00\r\n");
		} else {
			/* 2745 */ this.output.write("\r\n--00content0boundary00\r\n");
		}
		/* 2746 */ return this;
	}

	protected HttpRequest writePartHeader(String name, String filename) throws IOException {
		/* 2759 */ return writePartHeader(name, filename, null);
	}

	protected HttpRequest writePartHeader(String name, String filename, String contentType) throws IOException {
		/* 2773 */ StringBuilder partBuffer = new StringBuilder();
		/* 2774 */ partBuffer.append("form-data; name=\"").append(name);
		/* 2775 */ if (filename != null)
			/* 2776 */ partBuffer.append("\"; filename=\"").append(filename);
		/* 2777 */ partBuffer.append('"');
		/* 2778 */ partHeader("Content-Disposition", partBuffer.toString());
		/* 2779 */ if (contentType != null)
			/* 2780 */ partHeader("Content-Type", contentType);
		/* 2781 */ return send("\r\n");
	}

	public HttpRequest part(String name, String part) {
		/* 2792 */ return part(name, null, part);
	}

	public HttpRequest part(String name, String filename, String part) throws HttpRequest.HttpRequestException {
		/* 2806 */ return part(name, filename, null, part);
	}

	public HttpRequest part(String name, String filename, String contentType, String part)
			throws HttpRequest.HttpRequestException {
		try {
			/* 2823 */ startPart();
			/* 2824 */ writePartHeader(name, filename, contentType);
			/* 2825 */ this.output.write(part);
		} catch (IOException e) {
			/* 2827 */ throw new HttpRequestException(e);
		}
		/* 2829 */ return this;
	}

	public HttpRequest part(String name, Number part) throws HttpRequest.HttpRequestException {
		/* 2842 */ return part(name, null, part);
	}

	public HttpRequest part(String name, String filename, Number part) throws HttpRequest.HttpRequestException {
		/* 2856 */ return part(name, filename, part != null ? part.toString() : null);
	}

	public HttpRequest part(String name, File part) throws HttpRequest.HttpRequestException {
		/* 2869 */ return part(name, null, part);
	}

	public HttpRequest part(String name, String filename, File part) throws HttpRequest.HttpRequestException {
		/* 2883 */ return part(name, filename, null, part);
	}

	public HttpRequest part(String name, String filename, String contentType, File part)
			throws HttpRequest.HttpRequestException {
		try {
			/* 2901 */ InputStream stream = new BufferedInputStream(new FileInputStream(part));
			/* 2902 */ incrementTotalSize(part.length());
		} catch (IOException e) {
			/* 2904 */ throw new HttpRequestException(e);
		}
		InputStream stream;
		/* 2906 */ return part(name, filename, contentType, stream);
	}

	public HttpRequest part(String name, InputStream part) throws HttpRequest.HttpRequestException {
		/* 2919 */ return part(name, null, null, part);
	}

	public HttpRequest part(String name, String filename, String contentType, InputStream part)
			throws HttpRequest.HttpRequestException {
		try {
			/* 2937 */ startPart();
			/* 2938 */ writePartHeader(name, filename, contentType);
			/* 2939 */ copy(part, this.output);
		} catch (IOException e) {
			/* 2941 */ throw new HttpRequestException(e);
		}
		/* 2943 */ return this;
	}

	public HttpRequest partHeader(String name, String value) throws HttpRequest.HttpRequestException {
		/* 2956 */ return send(name).send(": ").send(value).send("\r\n");
	}

	public HttpRequest send(File input) throws HttpRequest.HttpRequestException {
		try {
			/* 2969 */ InputStream stream = new BufferedInputStream(new FileInputStream(input));
			/* 2970 */ incrementTotalSize(input.length());
		} catch (FileNotFoundException e) {
			/* 2972 */ throw new HttpRequestException(e);
		}
		InputStream stream;
		/* 2974 */ return send(stream);
	}

	public HttpRequest send(byte[] input) throws HttpRequest.HttpRequestException {
		/* 2985 */ if (input != null)
			/* 2986 */ incrementTotalSize(input.length);
		/* 2987 */ return send(new ByteArrayInputStream(input));
	}

	public HttpRequest send(InputStream input) throws HttpRequest.HttpRequestException {
		try {
			/* 3001 */ openOutput();
			/* 3002 */ copy(input, this.output);
		} catch (IOException e) {
			/* 3004 */ throw new HttpRequestException(e);
		}
		/* 3006 */ return this;
	}

	public HttpRequest send(final Reader input)
     throws HttpRequest.HttpRequestException
   {
     try
     {
/* 3020 */       openOutput();
     } catch (IOException e) {
/* 3022 */       throw new HttpRequestException(e);
     }
/* 3024 */     final Writer writer = new OutputStreamWriter(this.output, 
/* 3025 */       this.output.encoder.charset());
     
 
 
 
 
 
/* 3032 */     (HttpRequest)new FlushOperation(writer)
     {
       protected HttpRequest run() throws IOException
       {
/* 3030 */         return HttpRequest.this.copy(input, writer);
       }
     }.call();
   }

	public HttpRequest send(CharSequence value) throws HttpRequest.HttpRequestException {
		try {
			/* 3047 */ openOutput();
			/* 3048 */ this.output.write(value.toString());
		} catch (IOException e) {
			/* 3050 */ throw new HttpRequestException(e);
		}
		/* 3052 */ return this;
	}

	public OutputStreamWriter writer() throws HttpRequest.HttpRequestException {
		try {
			/* 3063 */ openOutput();
			/* 3064 */ return new OutputStreamWriter(this.output, this.output.encoder.charset());
		} catch (IOException e) {
			/* 3066 */ throw new HttpRequestException(e);
		}
	}

	public HttpRequest form(Map<?, ?> values) throws HttpRequest.HttpRequestException {
		/* 3081 */ return form(values, "UTF-8");
	}

	public HttpRequest form(Map.Entry<?, ?> entry) throws HttpRequest.HttpRequestException {
		/* 3095 */ return form(entry, "UTF-8");
	}

	public HttpRequest form(Map.Entry<?, ?> entry, String charset) throws HttpRequest.HttpRequestException {
		/* 3111 */ return form(entry.getKey(), entry.getValue(), charset);
	}

	public HttpRequest form(Object name, Object value) throws HttpRequest.HttpRequestException {
		/* 3127 */ return form(name, value, "UTF-8");
	}

	public HttpRequest form(Object name, Object value, String charset) throws HttpRequest.HttpRequestException {
		/* 3144 */ boolean first = !this.form;
		/* 3145 */ if (first) {
			/* 3146 */ contentType("application/x-www-form-urlencoded", charset);
			/* 3147 */ this.form = true;
		}
		/* 3149 */ charset = getValidCharset(charset);
		try {
			/* 3151 */ openOutput();
			/* 3152 */ if (!first)
				/* 3153 */ this.output.write(38);
			/* 3154 */ this.output.write(URLEncoder.encode(name.toString(), charset));
			/* 3155 */ this.output.write(61);
			/* 3156 */ if (value != null)
				/* 3157 */ this.output.write(URLEncoder.encode(value.toString(), charset));
		} catch (IOException e) {
			/* 3159 */ throw new HttpRequestException(e);
		}
		/* 3161 */ return this;
	}

	public HttpRequest form(Map<?, ?> values, String charset) throws HttpRequest.HttpRequestException {
		/* 3174 */ if (!values.isEmpty())
			/* 3175 */ for (Map.Entry<?, ?> entry : values.entrySet())
				/* 3176 */ form(entry, charset);
		/* 3177 */ return this;
	}

	public HttpRequest trustAllCerts() throws HttpRequest.HttpRequestException {
		/* 3189 */ HttpURLConnection connection = getConnection();
		/* 3190 */ if ((connection instanceof HttpsURLConnection)) {
			/* 3192 */ ((HttpsURLConnection) connection).setSSLSocketFactory(getTrustedFactory());
		}
		/* 3193 */ return this;
	}

	public HttpRequest trustAllHosts() {
		/* 3206 */ HttpURLConnection connection = getConnection();
		/* 3207 */ if ((connection instanceof HttpsURLConnection)) {
			/* 3209 */ ((HttpsURLConnection) connection).setHostnameVerifier(getTrustedVerifier());
		}
		/* 3210 */ return this;
	}

	public URL url() {
		/* 3219 */ return getConnection().getURL();
	}

	public String method() {
		/* 3228 */ return getConnection().getRequestMethod();
	}

	public HttpRequest useProxy(String proxyHost, int proxyPort) {
		/* 3240 */ if (this.connection != null) {
			/* 3241 */ throw new IllegalStateException(
					"The connection has already been created. This method must be called before reading or writing to the request.");
		}
		/* 3243 */ this.httpProxyHost = proxyHost;
		/* 3244 */ this.httpProxyPort = proxyPort;
		/* 3245 */ return this;
	}

	public HttpRequest followRedirects(boolean followRedirects) {
		/* 3256 */ getConnection().setInstanceFollowRedirects(followRedirects);
		/* 3257 */ return this;
	}
}

/*
 * Location:
 * /Users/8go/Desktop/1/vniapp_updater.jar!/com/github/kevinsawicki/http/
 * HttpRequest.class Java compiler version: 7 (51.0) JD-Core Version: 0.7.1
 */