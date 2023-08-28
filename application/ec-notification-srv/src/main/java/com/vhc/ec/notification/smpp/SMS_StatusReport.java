package com.vhc.ec.notification.smpp;

import java.io.IOException;
import java.net.URISyntaxException;

import com.vhc.ec.notification.config.SmppConfig;
import com.vhc.ec.notification.smpp.dto.SmsDeliverSMResp;
import com.vhc.ec.notification.smpp.dto.SmsSubmitResp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SMS_StatusReport {

	private static Logger logger = LoggerFactory.getLogger(SMS_StatusReport.class);

    public final static int Sequence_Style = 1;
    public final static int Concurrency_Style = 2;

    public static int OTPSendStyle = Sequence_Style;

    /**
     * Report sms_submit_sm status to handler
     *
     * @param config
     * @param obj
     * @throws URISyntaxException
     * @throws IOException
     */
    public static boolean submitSMStatusReport(SmppConfig config, SmsSubmitResp obj) throws URISyntaxException, IOException, Exception {

    	logger.info("submit sms status: " + obj);

        if (config == null || obj == null)
            return false;

        return obj.isSubmitSuccess;

        /*if (SMS.IsOTP(obj.SMS_TYPE)) {
            LocalLogger.Info(LocalLogger.SmsGwName, LocalLogger.OTPName, " Sending SubmitSM Status: " + obj.SMS_ID + " " + obj.IsSubmitSuccess);

            if (OTPSendStyle == Sequence_Style) {
                Exception tmpEx = null;
                for (int i = 0; i < 2 * config.OTP_SMS_STATUS_REPORT_URL.length; i++) {
                    try {
                        URI uri = new URIBuilder(config.OTP_SMS_STATUS_REPORT_URL[randomizer.nextInt(config.OTP_SMS_STATUS_REPORT_URL.length)])
                                .addParameter("api_key", config.OTP_SMS_STATUS_REPORT_APIKEY)
                                .addParameter("message_id", obj.SMS_ID)
                                .addParameter("status", obj.IsSubmitSuccess ? "SUBMIT_SUCCESS" : "SUBMIT_FAIL")
                                .addParameter("error", obj.Error)
                                .build();

                        HttpResult res = doHttpRequest(uri, config.OTP_SMS_STATUS_REPORT_TIMEOUT_INSEC);
                        if (res != null && res.body != null) {
                            LocalLogger.Info(LocalLogger.OTPName, LocalLogger.SmsGwName, "OTP response from " + res.address + " : " + res.body);

                            if (res.body.contains("\"status\":\"success\""))
                                return true;
                        }

                        if (res != null && res.exception != null)
                            tmpEx = res.exception;
                    } catch (Exception ex) {
                        tmpEx = ex;
                    }
                }

                if (tmpEx != null)
                    throw tmpEx;

                return false;
            } else {
                DoHttpRequestWithTimeout[] arr = new DoHttpRequestWithTimeout[config.OTP_SMS_STATUS_REPORT_URL.length];
                for (int i = 0; i < arr.length; i++) {
                    URI uri = new URIBuilder(config.OTP_SMS_STATUS_REPORT_URL[i])
                            .addParameter("api_key", config.OTP_SMS_STATUS_REPORT_APIKEY)
                            .addParameter("message_id", obj.SMS_ID)
                            .addParameter("status", obj.IsSubmitSuccess ? "SUBMIT_SUCCESS" : "SUBMIT_FAIL")
                            .addParameter("error", obj.Error)
                            .build();
                    arr[i] = new DoHttpRequestWithTimeout(uri, config.OTP_SMS_STATUS_REPORT_TIMEOUT_INSEC);
                }

                Exception tmpEx = null;
                ExecutorService executor = Executors.newWorkStealingPool();

                boolean finalResult = false;
                HttpResult[] st = executor.invokeAll(Arrays.asList(arr))
                        .stream()
                        .map(future -> {
                            try {
                                return future.get(config.OTP_SMS_STATUS_REPORT_TIMEOUT_INSEC + 1, TimeUnit.SECONDS);
                            } catch (Exception e) {
                                return null;
                            }
                        })
                        .filter(ob -> ob != null)
                        .toArray(size -> new HttpResult[size]);

                for (HttpResult hr : st) {
                    if (hr.body != null && hr.body.contains("\"status\":\"success\""))
                        return true;

                    LocalLogger.Info(LocalLogger.OTPName, LocalLogger.SmsGwName, "OTP response from " + hr.address + " : " + hr.body);

                    if (hr.exception != null)
                        tmpEx = hr.exception;
                }

                if (tmpEx != null)
                    throw tmpEx;

                return false;
            }
        }*/

        /*if (SMS.IsLeadGen(obj.SMS_TYPE)) {
            LocalLogger.Info(LocalLogger.SmsGwName, LocalLogger.SmsTrackerName, " Sending SubmitSM Status: " + obj.ISDN + " " + obj.SMS_ID + " " + obj.IsSubmitSuccess);

            for (int i = 0; i < config.SmsTracker_URL.length; i++) {
                try {
                    String url = config.SmsTracker_URL[i];
                    HttpPost post = new HttpPost(url + "/lead_delivery_status");

                    List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
                    urlParameters.add(new BasicNameValuePair("phone_number", obj.ISDN));
                    urlParameters.add(new BasicNameValuePair("campaign", obj.SMS_ID));
                    urlParameters.add(new BasicNameValuePair("status", obj.IsSubmitSuccess ? "SUBMIT_SUCCESS" : "SUBMIT_FAIL"));

                    post.setEntity(new UrlEncodedFormEntity(urlParameters));

                    HttpResponse resp = HttpUtils.DoHttpPost(post, config.SmsTracker_TIMEOUT_INSEC);
                    BasicResponseHandler handler = new BasicResponseHandler();
                    String body = handler.handleResponse(resp);

                    LocalLogger.Info(LocalLogger.SmsTrackerName, LocalLogger.SmsGwName, " Tracker response: " + body);

                    break;
                } catch (Exception ex) {
                }
            }
        }*/

        /*if (SMS.IsQuestion(obj.SMS_TYPE)) {
            LocalLogger.Info(LocalLogger.SmsGwName, LocalLogger.SmsTrackerName, " Sending Question Status: " + obj.ISDN + " " + obj.SMS_ID + " " + obj.IsSubmitSuccess);
            for (int i = 0; i < config.SmsTracker_URL.length; i++) {
                try {
                    String url = config.SmsTracker_URL[i];
                    HttpPost post = new HttpPost(url + "/question_delivery_status");

                    List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
                    urlParameters.add(new BasicNameValuePair("question_id", obj.SMS_ID));
                    urlParameters.add(new BasicNameValuePair("status", obj.IsSubmitSuccess ? "SUBMIT_SUCCESS" : "SUBMIT_FAIL"));

                    post.setEntity(new UrlEncodedFormEntity(urlParameters));

                    HttpResponse resp = HttpUtils.DoHttpPost(post, config.SmsTracker_TIMEOUT_INSEC);
                    BasicResponseHandler handler = new BasicResponseHandler();
                    String body = handler.handleResponse(resp);

                    LocalLogger.Info(LocalLogger.SmsTrackerName, LocalLogger.SmsGwName, " Tracker question response: " + body);

                    break;
                } catch (Exception ex) {
                }
            }
        }*/

        /*if (SMS.IsGeneral(obj.SMS_TYPE)) {
            LocalLogger.Info(LocalLogger.SmsGwName, LocalLogger.SmsTrackerName, " Sending GeneralSMS Status: " + obj.ISDN + " " + obj.SMS_ID + " " + obj.IsSubmitSuccess);

            for (int i = 0; i < config.SmsTracker_URL.length; i++) {
                try {
                    String url = config.SmsTracker_URL[i];
                    HttpPost post = new HttpPost(url + "/general_sms_delivery_status");

                    List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
                    urlParameters.add(new BasicNameValuePair("id", obj.SMS_ID));
                    urlParameters.add(new BasicNameValuePair("status", obj.IsSubmitSuccess ? "SUBMIT_SUCCESS" : "SUBMIT_FAIL"));
                    urlParameters.add(new BasicNameValuePair("error", obj.Error));

                    post.setEntity(new UrlEncodedFormEntity(urlParameters));

                    HttpResponse resp = HttpUtils.DoHttpPost(post, config.SmsTracker_TIMEOUT_INSEC);
                    BasicResponseHandler handler = new BasicResponseHandler();
                    String body = handler.handleResponse(resp);

                    LocalLogger.Info(LocalLogger.SmsTrackerName, LocalLogger.SmsGwName, " Tracker GeneralSMS response: " + body);

                    break;
                } catch (Exception ex) {
                }
            }
        }*/
    }

    /**
     * Report sms_submit_sm status to handler
     *
     * @param config
     * @param obj
     * @throws URISyntaxException
     * @throws IOException
     */
    public static boolean deliverSMStatusReport(SmppConfig config, SmsDeliverSMResp obj) throws URISyntaxException, IOException, Exception {

    	/*if (SMS.IsOTP(obj.SMS_TYPE)) {
            LocalLogger.Info(LocalLogger.SmsGwName, LocalLogger.OTPName, " Sending Delivery Status: " + obj.SMS_ID + " " + obj.IsSent);

            if (OTPSendStyle == Sequence_Style) {
                Exception tmpEx = null;
                for (int i = 0; i < 2 * config.OTP_SMS_STATUS_REPORT_URL.length; i++) {
                    try {
                        URI uri = new URIBuilder(config.OTP_SMS_STATUS_REPORT_URL[randomizer.nextInt(config.OTP_SMS_STATUS_REPORT_URL.length)])
                                .addParameter("api_key", config.OTP_SMS_STATUS_REPORT_APIKEY)
                                .addParameter("message_id", obj.SMS_ID)
                                .addParameter("status", obj.IsSent ? "SENT_SUCCESS" : "SENT_FAIL")
                                .addParameter("error", obj.Error)
                                .build();

                        HttpResult res = doHttpRequest(uri, config.OTP_SMS_STATUS_REPORT_TIMEOUT_INSEC);
                        if (res != null && res.body != null) {
                            LocalLogger.Info(LocalLogger.OTPName, LocalLogger.SmsGwName, "OTP response from " + res.address + " : " + res.body);

                            if (res.body.contains("\"status\":\"success\""))
                                return true;
                        }

                        if (res != null && res.exception != null)
                            tmpEx = res.exception;
                    } catch (Exception ex) {
                        tmpEx = ex;
                    }
                }

                if (tmpEx != null)
                    throw tmpEx;

                return false;
            } else {
                DoHttpRequestWithTimeout[] arr = new DoHttpRequestWithTimeout[config.OTP_SMS_STATUS_REPORT_URL.length];
                for (int i = 0; i < arr.length; i++) {
                    URI uri = new URIBuilder(config.OTP_SMS_STATUS_REPORT_URL[i])
                            .addParameter("api_key", config.OTP_SMS_STATUS_REPORT_APIKEY)
                            .addParameter("message_id", obj.SMS_ID)
                            .addParameter("status", obj.IsSent ? "SENT_SUCCESS" : "SENT_FAIL")
                            .addParameter("error", obj.Error)
                            .build();
                    arr[i] = new DoHttpRequestWithTimeout(uri, config.OTP_SMS_STATUS_REPORT_TIMEOUT_INSEC);
                }

                Exception tmpEx = null;
                ExecutorService executor = Executors.newWorkStealingPool();

                boolean finalResult = false;
                HttpResult[] st = executor.invokeAll(Arrays.asList(arr))
                        .stream()
                        .map(future -> {
                            try {
                                return future.get(config.OTP_SMS_STATUS_REPORT_TIMEOUT_INSEC + 1, TimeUnit.SECONDS);
                            } catch (Exception e) {
                                return null;
                            }
                        })
                        .filter(ob -> ob != null)
                        .toArray(size -> new HttpResult[size]);

                for (HttpResult hr : st) {
                    if (hr.body != null && hr.body.contains("\"status\":\"success\""))
                        return true;

                    LocalLogger.Info(LocalLogger.OTPName, LocalLogger.SmsGwName, "OTP response from " + hr.address + " : " + hr.body);

                    if (hr.exception != null)
                        tmpEx = hr.exception;
                }

                if (tmpEx != null)
                    throw tmpEx;

                return false;
            }
        }*/

        /*if (SMS.IsLeadGen(obj.SMS_TYPE)) {
            LocalLogger.Info(LocalLogger.SmsGwName, LocalLogger.SmsTrackerName, " Sending Delivery Status: " + obj.ISDN + " " + obj.SMS_ID + " " + obj.IsSent);

            for (int i = 0; i < config.SmsTracker_URL.length; i++) {
                try {
                    String url = config.SmsTracker_URL[i];

                    HttpPost post = new HttpPost(url + "/lead_delivery_status");

                    List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
                    urlParameters.add(new BasicNameValuePair("phone_number", obj.ISDN));
                    urlParameters.add(new BasicNameValuePair("campaign", obj.SMS_ID));
                    urlParameters.add(new BasicNameValuePair("status", obj.IsSent ? "SENT_SUCCESS" : "SENT_FAIL"));

                    post.setEntity(new UrlEncodedFormEntity(urlParameters));

                    HttpResponse resp = HttpUtils.DoHttpPost(post, config.SmsTracker_TIMEOUT_INSEC);
                    BasicResponseHandler handler = new BasicResponseHandler();
                    String body = handler.handleResponse(resp);

                    LocalLogger.Info(LocalLogger.SmsTrackerName, LocalLogger.SmsGwName, " Tracker response: " + body);

                    break;
                } catch (Exception ex) {
                }
            }
        }*/

        /*if (SMS.IsQuestion(obj.SMS_TYPE)) {
            LocalLogger.Info(LocalLogger.SmsGwName, LocalLogger.SmsTrackerName, " Sending Delivery Question Status: " + obj.ISDN + " " + obj.SMS_ID + " " + obj.IsSent);

            for (int i = 0; i < config.SmsTracker_URL.length; i++) {
                try {
                    String url = config.SmsTracker_URL[i];
                    HttpPost post = new HttpPost(url + "/question_delivery_status");

                    List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
                    urlParameters.add(new BasicNameValuePair("question_id", obj.SMS_ID));
                    urlParameters.add(new BasicNameValuePair("status", obj.IsSent ? "SENT_SUCCESS" : "SENT_FAIL"));

                    post.setEntity(new UrlEncodedFormEntity(urlParameters));

                    HttpResponse resp = HttpUtils.DoHttpPost(post, config.SmsTracker_TIMEOUT_INSEC);
                    BasicResponseHandler handler = new BasicResponseHandler();
                    String body = handler.handleResponse(resp);

                    LocalLogger.Info(LocalLogger.SmsTrackerName, LocalLogger.SmsGwName, " Tracker question response: " + body);

                    break;
                } catch (Exception ex) {
                }
            }
        }*/

        /*if (SMS.IsGeneral(obj.SMS_TYPE)) {
            LocalLogger.Info(LocalLogger.SmsGwName, LocalLogger.SmsTrackerName, " Sending Delivery Question Status: " + obj.ISDN + " " + obj.SMS_ID + " " + obj.IsSent);

            for (int i = 0; i < config.SmsTracker_URL.length; i++) {
                try {
                    String url = config.SmsTracker_URL[i];
                    HttpPost post = new HttpPost(url + "/general_sms_delivery_status");

                    List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
                    urlParameters.add(new BasicNameValuePair("id", obj.SMS_ID));
                    urlParameters.add(new BasicNameValuePair("status", obj.IsSent ? "SENT_SUCCESS" : "SENT_FAIL"));
                    urlParameters.add(new BasicNameValuePair("error", obj.Error));

                    post.setEntity(new UrlEncodedFormEntity(urlParameters));

                    HttpResponse resp = HttpUtils.DoHttpPost(post, config.SmsTracker_TIMEOUT_INSEC);
                    BasicResponseHandler handler = new BasicResponseHandler();
                    String body = handler.handleResponse(resp);

                    LocalLogger.Info(LocalLogger.SmsTrackerName, LocalLogger.SmsGwName, " Tracker question response: " + body);

                    break;
                } catch (Exception ex) {
                }
            }
        }*/

        return true;
    }

    /*private static HttpResult doHttpRequest(URI uri, int timeout) {

        try {
            HttpGet request = new HttpGet();
            request.setURI(uri);

            HttpResponse resp = HttpClientUtil.doHttpGet(request, timeout);
            BasicResponseHandler handler = new BasicResponseHandler();

            return new HttpResult(uri.getHost() + ":" + uri.getPort(), "" + handler.handleResponse(resp), null);
        } catch (Exception ex) {
            return new HttpResult(uri.getHost() + ":" + uri.getPort(), "", ex);
        }
    }*/

    static class HttpResult {
        public String body;
        public String address;
        public Exception exception;

        public HttpResult(String fromAddress, String body, Exception exp) {
            this.body = body;
            this.exception = exp;
            this.address = fromAddress;
        }
    }

    /*static class DoHttpRequestWithTimeout implements Callable<HttpResult> {
        private URI uri;
        private int timeout;

        public DoHttpRequestWithTimeout(URI uri, int timeout) {
            this.uri = uri;
            this.timeout = timeout;
        }

        @Override
        public HttpResult call() {
        	try {
        		HttpResponse resp = HttpClientUtil.doHttpGet(this.uri, this.timeout);

            	BasicResponseHandler handler = new BasicResponseHandler();

                return new HttpResult(uri.getHost() + ":" + uri.getPort(), "" + handler.handleResponse(resp), null);
        	} catch (Exception e) {
        		return new HttpResult(uri.getHost() + ":" + uri.getPort(), "", e);
        	}
        }
    }*/
}
