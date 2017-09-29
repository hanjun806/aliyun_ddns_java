package com.vniapp;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.File;
/*     */ import java.io.InputStreamReader;
/*     */ import java.io.PrintStream;
/*     */ import java.util.List;
/*     */ import java.util.regex.Matcher;
/*     */ import java.util.regex.Pattern;
/*     */ import org.apache.http.Header;
/*     */ import org.apache.http.HttpResponse;
/*     */ import org.apache.http.conn.ClientConnectionManager;
/*     */ import org.apache.http.impl.client.DefaultHttpClient;
/*     */ import org.jsoup.Jsoup;
/*     */ import org.jsoup.nodes.Document;
/*     */ import org.jsoup.nodes.Element;
/*     */ import org.jsoup.select.Elements;
/*     */ import org.nutz.http.Http;
/*     */ import org.nutz.http.Response;
/*     */ import org.nutz.lang.Files;
/*     */ import org.nutz.log.Log;
/*     */ import org.nutz.log.Logs;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class UpdateIP
/*     */ {
/*  32 */   Log log = Logs.get();
/*     */   
/*  34 */   private static String renRenLoginURL = "http://www.myhostadmin.net/domain/checklogin.asp";
/*     */   private HttpResponse response;
/*  36 */   private DefaultHttpClient httpclient = new DefaultHttpClient();
/*     */   
/*     */   /* Error */
/*     */   private boolean login()
/*     */   {
/*     */     // Byte code:
/*     */     //   0: aload_0
/*     */     //   1: getfield 31	UpdateIP:log	Lorg/nutz/log/Log;
/*     */     //   4: ldc 42
/*     */     //   6: invokeinterface 44 2 0
/*     */     //   11: new 50	org/apache/http/client/methods/HttpPost
/*     */     //   14: dup
/*     */     //   15: getstatic 18	UpdateIP:renRenLoginURL	Ljava/lang/String;
/*     */     //   18: invokespecial 52	org/apache/http/client/methods/HttpPost:<init>	(Ljava/lang/String;)V
/*     */     //   21: astore_1
/*     */     //   22: new 55	java/util/ArrayList
/*     */     //   25: dup
/*     */     //   26: invokespecial 57	java/util/ArrayList:<init>	()V
/*     */     //   29: astore_2
/*     */     //   30: aload_2
/*     */     //   31: new 58	org/apache/http/message/BasicNameValuePair
/*     */     //   34: dup
/*     */     //   35: ldc 60
/*     */     //   37: ldc 62
/*     */     //   39: invokespecial 64	org/apache/http/message/BasicNameValuePair:<init>	(Ljava/lang/String;Ljava/lang/String;)V
/*     */     //   42: invokeinterface 67 2 0
/*     */     //   47: pop
/*     */     //   48: aload_2
/*     */     //   49: new 58	org/apache/http/message/BasicNameValuePair
/*     */     //   52: dup
/*     */     //   53: ldc 73
/*     */     //   55: ldc 75
/*     */     //   57: invokespecial 64	org/apache/http/message/BasicNameValuePair:<init>	(Ljava/lang/String;Ljava/lang/String;)V
/*     */     //   60: invokeinterface 67 2 0
/*     */     //   65: pop
/*     */     //   66: aload_2
/*     */     //   67: new 58	org/apache/http/message/BasicNameValuePair
/*     */     //   70: dup
/*     */     //   71: ldc 77
/*     */     //   73: ldc 62
/*     */     //   75: invokespecial 64	org/apache/http/message/BasicNameValuePair:<init>	(Ljava/lang/String;Ljava/lang/String;)V
/*     */     //   78: invokeinterface 67 2 0
/*     */     //   83: pop
/*     */     //   84: aload_2
/*     */     //   85: new 58	org/apache/http/message/BasicNameValuePair
/*     */     //   88: dup
/*     */     //   89: ldc 79
/*     */     //   91: ldc 81
/*     */     //   93: invokespecial 64	org/apache/http/message/BasicNameValuePair:<init>	(Ljava/lang/String;Ljava/lang/String;)V
/*     */     //   96: invokeinterface 67 2 0
/*     */     //   101: pop
/*     */     //   102: aload_2
/*     */     //   103: new 58	org/apache/http/message/BasicNameValuePair
/*     */     //   106: dup
/*     */     //   107: ldc 83
/*     */     //   109: ldc 85
/*     */     //   111: invokespecial 64	org/apache/http/message/BasicNameValuePair:<init>	(Ljava/lang/String;Ljava/lang/String;)V
/*     */     //   114: invokeinterface 67 2 0
/*     */     //   119: pop
/*     */     //   120: aload_1
/*     */     //   121: new 87	org/apache/http/client/entity/UrlEncodedFormEntity
/*     */     //   124: dup
/*     */     //   125: aload_2
/*     */     //   126: ldc 89
/*     */     //   128: invokespecial 91	org/apache/http/client/entity/UrlEncodedFormEntity:<init>	(Ljava/util/List;Ljava/lang/String;)V
/*     */     //   131: invokevirtual 94	org/apache/http/client/methods/HttpPost:setEntity	(Lorg/apache/http/HttpEntity;)V
/*     */     //   134: aload_0
/*     */     //   135: aload_0
/*     */     //   136: getfield 36	UpdateIP:httpclient	Lorg/apache/http/impl/client/DefaultHttpClient;
/*     */     //   139: aload_1
/*     */     //   140: invokevirtual 98	org/apache/http/impl/client/DefaultHttpClient:execute	(Lorg/apache/http/client/methods/HttpUriRequest;)Lorg/apache/http/client/methods/CloseableHttpResponse;
/*     */     //   143: putfield 102	UpdateIP:response	Lorg/apache/http/HttpResponse;
/*     */     //   146: aload_0
/*     */     //   147: getfield 31	UpdateIP:log	Lorg/nutz/log/Log;
/*     */     //   150: ldc 104
/*     */     //   152: invokeinterface 44 2 0
/*     */     //   157: goto +34 -> 191
/*     */     //   160: astore_3
/*     */     //   161: aload_3
/*     */     //   162: invokevirtual 106	java/lang/Exception:printStackTrace	()V
/*     */     //   165: aload_0
/*     */     //   166: getfield 31	UpdateIP:log	Lorg/nutz/log/Log;
/*     */     //   169: ldc 111
/*     */     //   171: invokeinterface 44 2 0
/*     */     //   176: aload_1
/*     */     //   177: invokevirtual 113	org/apache/http/client/methods/HttpPost:abort	()V
/*     */     //   180: iconst_0
/*     */     //   181: ireturn
/*     */     //   182: astore 4
/*     */     //   184: aload_1
/*     */     //   185: invokevirtual 113	org/apache/http/client/methods/HttpPost:abort	()V
/*     */     //   188: aload 4
/*     */     //   190: athrow
/*     */     //   191: aload_1
/*     */     //   192: invokevirtual 113	org/apache/http/client/methods/HttpPost:abort	()V
/*     */     //   195: iconst_1
/*     */     //   196: ireturn
/*     */     // Line number table:
/*     */     //   Java source line #39	-> byte code offset #0
/*     */     //   Java source line #40	-> byte code offset #11
/*     */     //   Java source line #42	-> byte code offset #22
/*     */     //   Java source line #43	-> byte code offset #30
/*     */     //   Java source line #44	-> byte code offset #48
/*     */     //   Java source line #45	-> byte code offset #66
/*     */     //   Java source line #46	-> byte code offset #84
/*     */     //   Java source line #47	-> byte code offset #102
/*     */     //   Java source line #49	-> byte code offset #120
/*     */     //   Java source line #50	-> byte code offset #134
/*     */     //   Java source line #52	-> byte code offset #146
/*     */     //   Java source line #54	-> byte code offset #157
/*     */     //   Java source line #55	-> byte code offset #161
/*     */     //   Java source line #57	-> byte code offset #165
/*     */     //   Java source line #61	-> byte code offset #176
/*     */     //   Java source line #59	-> byte code offset #180
/*     */     //   Java source line #60	-> byte code offset #182
/*     */     //   Java source line #61	-> byte code offset #184
/*     */     //   Java source line #62	-> byte code offset #188
/*     */     //   Java source line #61	-> byte code offset #191
/*     */     //   Java source line #63	-> byte code offset #195
/*     */     // Local variable table:
/*     */     //   start	length	slot	name	signature
/*     */     //   0	197	0	this	UpdateIP
/*     */     //   21	171	1	httpost	org.apache.http.client.methods.HttpPost
/*     */     //   29	97	2	nvps	List<org.apache.http.NameValuePair>
/*     */     //   160	2	3	e	Exception
/*     */     //   182	7	4	localObject	Object
/*     */     // Exception table:
/*     */     //   from	to	target	type
/*     */     //   120	157	160	java/lang/Exception
/*     */     //   120	176	182	finally
/*     */   }
/*     */   
/*     */   private String getRedirectLocation()
/*     */   {
/*  67 */     Header locationHeader = this.response.getFirstHeader("Location");
/*  68 */     if (locationHeader == null) {
/*  69 */       return null;
/*     */     }
/*  71 */     return locationHeader.getValue();
/*     */   }
/*     */   
/*     */   /* Error */
/*     */   private String getText(String redirectLocation)
/*     */   {
/*     */     // Byte code:
/*     */     //   0: new 146	org/apache/http/client/methods/HttpGet
/*     */     //   3: dup
/*     */     //   4: aload_1
/*     */     //   5: invokespecial 148	org/apache/http/client/methods/HttpGet:<init>	(Ljava/lang/String;)V
/*     */     //   8: astore_2
/*     */     //   9: new 149	org/apache/http/impl/client/BasicResponseHandler
/*     */     //   12: dup
/*     */     //   13: invokespecial 151	org/apache/http/impl/client/BasicResponseHandler:<init>	()V
/*     */     //   16: astore_3
/*     */     //   17: ldc 85
/*     */     //   19: astore 4
/*     */     //   21: aload_0
/*     */     //   22: getfield 36	UpdateIP:httpclient	Lorg/apache/http/impl/client/DefaultHttpClient;
/*     */     //   25: aload_2
/*     */     //   26: aload_3
/*     */     //   27: invokevirtual 152	org/apache/http/impl/client/DefaultHttpClient:execute	(Lorg/apache/http/client/methods/HttpUriRequest;Lorg/apache/http/client/ResponseHandler;)Ljava/lang/Object;
/*     */     //   30: checkcast 155	java/lang/String
/*     */     //   33: astore 4
/*     */     //   35: goto +29 -> 64
/*     */     //   38: astore 5
/*     */     //   40: aload 5
/*     */     //   42: invokevirtual 106	java/lang/Exception:printStackTrace	()V
/*     */     //   45: aconst_null
/*     */     //   46: astore 4
/*     */     //   48: aload_2
/*     */     //   49: invokevirtual 157	org/apache/http/client/methods/HttpGet:abort	()V
/*     */     //   52: goto +16 -> 68
/*     */     //   55: astore 6
/*     */     //   57: aload_2
/*     */     //   58: invokevirtual 157	org/apache/http/client/methods/HttpGet:abort	()V
/*     */     //   61: aload 6
/*     */     //   63: athrow
/*     */     //   64: aload_2
/*     */     //   65: invokevirtual 157	org/apache/http/client/methods/HttpGet:abort	()V
/*     */     //   68: aload 4
/*     */     //   70: areturn
/*     */     // Line number table:
/*     */     //   Java source line #75	-> byte code offset #0
/*     */     //   Java source line #77	-> byte code offset #9
/*     */     //   Java source line #78	-> byte code offset #17
/*     */     //   Java source line #80	-> byte code offset #21
/*     */     //   Java source line #81	-> byte code offset #35
/*     */     //   Java source line #82	-> byte code offset #40
/*     */     //   Java source line #83	-> byte code offset #45
/*     */     //   Java source line #85	-> byte code offset #48
/*     */     //   Java source line #84	-> byte code offset #55
/*     */     //   Java source line #85	-> byte code offset #57
/*     */     //   Java source line #86	-> byte code offset #61
/*     */     //   Java source line #85	-> byte code offset #64
/*     */     //   Java source line #87	-> byte code offset #68
/*     */     // Local variable table:
/*     */     //   start	length	slot	name	signature
/*     */     //   0	71	0	this	UpdateIP
/*     */     //   0	71	1	redirectLocation	String
/*     */     //   8	57	2	httpget	org.apache.http.client.methods.HttpGet
/*     */     //   16	11	3	responseHandler	org.apache.http.client.ResponseHandler<String>
/*     */     //   19	50	4	responseBody	String
/*     */     //   38	3	5	e	Exception
/*     */     //   55	7	6	localObject	Object
/*     */     // Exception table:
/*     */     //   from	to	target	type
/*     */     //   21	35	38	java/lang/Exception
/*     */     //   21	48	55	finally
/*     */   }
/*     */   
/*     */   public void updateIP(List<String> dns, String myIP)
/*     */   {
/*  91 */     if ((myIP != null) && (login())) {
/*  92 */       String redirectLocation = getRedirectLocation();
/*     */       
/*  94 */       if (redirectLocation != null) {
/*  95 */         redirectLocation = "http://www.myhostadmin.net" + redirectLocation;
/*  96 */         String homePage = getText(redirectLocation);
/*  97 */         String iplist = getText("http://www.myhostadmin.net/domain/rsall.asp");
/*     */         
/*  99 */         Document document = Jsoup.parse(iplist);
/* 100 */         Elements els = document.select(".Table_Div_dns tr");
/* 101 */         for (Element element : els)
/*     */         {
/* 103 */           String attrId = element.attr("id").replace("tr_", "");
/*     */           
/* 105 */           String host = element.select("td:eq(0)").text();
/* 106 */           String type = element.select("td:eq(1)").text();
/* 107 */           String value = element.select("td:eq(2)").text();
/*     */           
/* 109 */           if (dns.contains(host)) {
/* 110 */             this.log.info("Update " + host + " with " + type + " to " + myIP);
/* 111 */             modifyIp(attrId, host, type, value, myIP);
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/* 116 */     this.httpclient.getConnectionManager().shutdown();
/*     */   }
/*     */   
/*     */   /* Error */
/*     */   public void modifyIp(String rowId, String host, String type, String old, String newIP)
/*     */   {
/*     */     // Byte code:
/*     */     //   0: new 50	org/apache/http/client/methods/HttpPost
/*     */     //   3: dup
/*     */     //   4: ldc_w 279
/*     */     //   7: invokespecial 52	org/apache/http/client/methods/HttpPost:<init>	(Ljava/lang/String;)V
/*     */     //   10: astore 6
/*     */     //   12: new 55	java/util/ArrayList
/*     */     //   15: dup
/*     */     //   16: invokespecial 57	java/util/ArrayList:<init>	()V
/*     */     //   19: astore 7
/*     */     //   21: aload 7
/*     */     //   23: new 58	org/apache/http/message/BasicNameValuePair
/*     */     //   26: dup
/*     */     //   27: ldc_w 281
/*     */     //   30: aload_1
/*     */     //   31: invokespecial 64	org/apache/http/message/BasicNameValuePair:<init>	(Ljava/lang/String;Ljava/lang/String;)V
/*     */     //   34: invokeinterface 67 2 0
/*     */     //   39: pop
/*     */     //   40: aload 7
/*     */     //   42: new 58	org/apache/http/message/BasicNameValuePair
/*     */     //   45: dup
/*     */     //   46: ldc 83
/*     */     //   48: ldc_w 283
/*     */     //   51: invokespecial 64	org/apache/http/message/BasicNameValuePair:<init>	(Ljava/lang/String;Ljava/lang/String;)V
/*     */     //   54: invokeinterface 67 2 0
/*     */     //   59: pop
/*     */     //   60: aload 7
/*     */     //   62: new 58	org/apache/http/message/BasicNameValuePair
/*     */     //   65: dup
/*     */     //   66: ldc_w 285
/*     */     //   69: ldc_w 287
/*     */     //   72: invokespecial 64	org/apache/http/message/BasicNameValuePair:<init>	(Ljava/lang/String;Ljava/lang/String;)V
/*     */     //   75: invokeinterface 67 2 0
/*     */     //   80: pop
/*     */     //   81: aload 7
/*     */     //   83: new 58	org/apache/http/message/BasicNameValuePair
/*     */     //   86: dup
/*     */     //   87: ldc_w 289
/*     */     //   90: aload 4
/*     */     //   92: invokespecial 64	org/apache/http/message/BasicNameValuePair:<init>	(Ljava/lang/String;Ljava/lang/String;)V
/*     */     //   95: invokeinterface 67 2 0
/*     */     //   100: pop
/*     */     //   101: aload 7
/*     */     //   103: new 58	org/apache/http/message/BasicNameValuePair
/*     */     //   106: dup
/*     */     //   107: ldc_w 291
/*     */     //   110: aload_2
/*     */     //   111: invokespecial 64	org/apache/http/message/BasicNameValuePair:<init>	(Ljava/lang/String;Ljava/lang/String;)V
/*     */     //   114: invokeinterface 67 2 0
/*     */     //   119: pop
/*     */     //   120: aload 7
/*     */     //   122: new 58	org/apache/http/message/BasicNameValuePair
/*     */     //   125: dup
/*     */     //   126: ldc_w 293
/*     */     //   129: ldc_w 295
/*     */     //   132: invokespecial 64	org/apache/http/message/BasicNameValuePair:<init>	(Ljava/lang/String;Ljava/lang/String;)V
/*     */     //   135: invokeinterface 67 2 0
/*     */     //   140: pop
/*     */     //   141: aload 7
/*     */     //   143: new 58	org/apache/http/message/BasicNameValuePair
/*     */     //   146: dup
/*     */     //   147: ldc_w 297
/*     */     //   150: ldc_w 299
/*     */     //   153: invokespecial 64	org/apache/http/message/BasicNameValuePair:<init>	(Ljava/lang/String;Ljava/lang/String;)V
/*     */     //   156: invokeinterface 67 2 0
/*     */     //   161: pop
/*     */     //   162: aload 7
/*     */     //   164: new 58	org/apache/http/message/BasicNameValuePair
/*     */     //   167: dup
/*     */     //   168: ldc_w 301
/*     */     //   171: aload 5
/*     */     //   173: invokespecial 64	org/apache/http/message/BasicNameValuePair:<init>	(Ljava/lang/String;Ljava/lang/String;)V
/*     */     //   176: invokeinterface 67 2 0
/*     */     //   181: pop
/*     */     //   182: aload 6
/*     */     //   184: new 87	org/apache/http/client/entity/UrlEncodedFormEntity
/*     */     //   187: dup
/*     */     //   188: aload 7
/*     */     //   190: ldc 89
/*     */     //   192: invokespecial 91	org/apache/http/client/entity/UrlEncodedFormEntity:<init>	(Ljava/util/List;Ljava/lang/String;)V
/*     */     //   195: invokevirtual 94	org/apache/http/client/methods/HttpPost:setEntity	(Lorg/apache/http/HttpEntity;)V
/*     */     //   198: aload_0
/*     */     //   199: getfield 36	UpdateIP:httpclient	Lorg/apache/http/impl/client/DefaultHttpClient;
/*     */     //   202: aload 6
/*     */     //   204: invokevirtual 98	org/apache/http/impl/client/DefaultHttpClient:execute	(Lorg/apache/http/client/methods/HttpUriRequest;)Lorg/apache/http/client/methods/CloseableHttpResponse;
/*     */     //   207: invokeinterface 303 1 0
/*     */     //   212: invokeinterface 309 1 0
/*     */     //   217: pop
/*     */     //   218: getstatic 315	java/lang/System:out	Ljava/io/PrintStream;
/*     */     //   221: new 174	java/lang/StringBuilder
/*     */     //   224: dup
/*     */     //   225: ldc_w 321
/*     */     //   228: invokespecial 178	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
/*     */     //   231: aload_2
/*     */     //   232: invokevirtual 179	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*     */     //   235: invokevirtual 183	java/lang/StringBuilder:toString	()Ljava/lang/String;
/*     */     //   238: invokevirtual 323	java/io/PrintStream:println	(Ljava/lang/String;)V
/*     */     //   241: goto +28 -> 269
/*     */     //   244: astore 8
/*     */     //   246: aload 8
/*     */     //   248: invokevirtual 106	java/lang/Exception:printStackTrace	()V
/*     */     //   251: aload 6
/*     */     //   253: invokevirtual 113	org/apache/http/client/methods/HttpPost:abort	()V
/*     */     //   256: goto +18 -> 274
/*     */     //   259: astore 9
/*     */     //   261: aload 6
/*     */     //   263: invokevirtual 113	org/apache/http/client/methods/HttpPost:abort	()V
/*     */     //   266: aload 9
/*     */     //   268: athrow
/*     */     //   269: aload 6
/*     */     //   271: invokevirtual 113	org/apache/http/client/methods/HttpPost:abort	()V
/*     */     //   274: return
/*     */     // Line number table:
/*     */     //   Java source line #120	-> byte code offset #0
/*     */     //   Java source line #121	-> byte code offset #12
/*     */     //   Java source line #122	-> byte code offset #21
/*     */     //   Java source line #123	-> byte code offset #40
/*     */     //   Java source line #124	-> byte code offset #60
/*     */     //   Java source line #125	-> byte code offset #81
/*     */     //   Java source line #126	-> byte code offset #101
/*     */     //   Java source line #127	-> byte code offset #120
/*     */     //   Java source line #128	-> byte code offset #141
/*     */     //   Java source line #129	-> byte code offset #162
/*     */     //   Java source line #131	-> byte code offset #182
/*     */     //   Java source line #132	-> byte code offset #198
/*     */     //   Java source line #133	-> byte code offset #218
/*     */     //   Java source line #134	-> byte code offset #241
/*     */     //   Java source line #135	-> byte code offset #246
/*     */     //   Java source line #137	-> byte code offset #251
/*     */     //   Java source line #136	-> byte code offset #259
/*     */     //   Java source line #137	-> byte code offset #261
/*     */     //   Java source line #138	-> byte code offset #266
/*     */     //   Java source line #137	-> byte code offset #269
/*     */     //   Java source line #140	-> byte code offset #274
/*     */     // Local variable table:
/*     */     //   start	length	slot	name	signature
/*     */     //   0	275	0	this	UpdateIP
/*     */     //   0	275	1	rowId	String
/*     */     //   0	275	2	host	String
/*     */     //   0	275	3	type	String
/*     */     //   0	275	4	old	String
/*     */     //   0	275	5	newIP	String
/*     */     //   10	260	6	httpost	org.apache.http.client.methods.HttpPost
/*     */     //   19	170	7	nvps	List<org.apache.http.NameValuePair>
/*     */     //   244	3	8	e	Exception
/*     */     //   259	8	9	localObject	Object
/*     */     // Exception table:
/*     */     //   from	to	target	type
/*     */     //   182	241	244	java/lang/Exception
/*     */     //   182	251	259	finally
/*     */   }
/*     */   
/*     */   public static String myIp()
/*     */   {
/*     */     try
/*     */     {
/* 144 */       Response response = Http.get("http://1212.ip138.com/ic.asp", 10000);
/* 145 */       String html = response.getContent("GBK");
/* 146 */       Pattern pattern = Pattern.compile("\\[[^\\]]*\\]");
/* 147 */       Matcher m = pattern.matcher(html);
/* 148 */       if (m.find()) {
/* 149 */         return m.group().replace("[", "").replace("]", "");
/*     */       }
/*     */     } catch (Exception e) {
/* 152 */       e.printStackTrace();
/*     */     }
/* 154 */     return null;
/*     */   }
/*     */   
/*     */   public static void main(String[] args)
/*     */   {
/* 159 */     if (args.length == 0) {
/* 160 */       System.exit(0);
/*     */     }
/* 162 */     List<String> dns = Files.readLines(new File(args[0]));
/* 163 */     String myIp = myIp();
/* 164 */     if (myIp == null) {
/* 165 */       System.out.println("Cant get remote IP");
/* 166 */       System.exit(0);
/*     */     }
/*     */     
/* 169 */     UpdateIP loginAndSet = new UpdateIP();
/*     */     
/* 171 */     boolean needupdate = loginAndSet.ping((String)dns.get(0), myIp);
/*     */     
/*     */ 
/* 174 */     loginAndSet.updateIP(dns, myIp);
/*     */   }
/*     */   
/*     */   public boolean ping(String name, String newIp)
/*     */   {
/*     */     try {
/* 180 */       this.log.info("Ping Host");
/* 181 */       Process pro = Runtime.getRuntime().exec("ping " + name + ".vniapp.com");
/* 182 */       BufferedReader buf = new BufferedReader(new InputStreamReader(pro.getInputStream()));
/*     */       String line;
/* 184 */       while ((line = buf.readLine()) != null) { String line;
/* 185 */         Pattern p = Pattern.compile("\\d{1,3}.\\d{1,3}.\\d{1,3}.\\d{1,3}");
/* 186 */         Matcher matcher = p.matcher(line);
/* 187 */         if (matcher.find()) {
/* 188 */           String ip = matcher.group();
/* 189 */           this.log.info("Get the host IP " + ip);
/*     */           
/* 191 */           if (!newIp.equalsIgnoreCase(ip)) {
/* 192 */             this.log.info("Need Update the host to  " + newIp);
/* 193 */             return true;
/*     */           }
/* 195 */           this.log.info("Same ip keep it now ");
/* 196 */           return false;
/*     */         }
/*     */       }
/*     */       
/* 200 */       System.out.println(line);
/*     */     } catch (Exception ex) {
/* 202 */       System.out.println(ex.getMessage());
/*     */     }
/* 204 */     return true;
/*     */   }
/*     */ }


/* Location:              /Users/8go/Desktop/1/vniapp_updater.jar!/UpdateIP.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */