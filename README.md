
# TLS/SSL, Man-in-the-middle attack, Certificate Pinning

## Background
> The Secure Sockets Layer (SSL) — now technically known as [Transport Layer Security (TLS)](https://en.wikipedia.org/wiki/Transport_Layer_Security) — is a common building block for encrypted communications between clients and servers.
> In a typical SSL usage scenario, a server is configured with a certificate containing a public key as well as a matching private key. As part of the handshake between an SSL client and server, the server proves it has the private key by signing its certificate with [public-key cryptography](https://en.wikipedia.org/wiki/Public-key_cryptography).
> However, anyone can generate their own certificate and private key, so a simple handshake doesn’t prove anything about the server other than that the server knows the private key that matches the public key of the certificate. One way to solve this problem is to have the client have a set of one or more certificates it trusts. If the certificate is not in the set, the server is not to be trusted.
> There are several downsides to this simple approach. Servers should be able to upgrade to stronger keys over time (“key rotation”), which replaces the public key in the certificate with a new one. Unfortunately, now the client app has to be updated due to what is essentially a server configuration change. This is especially problematic if the server is not under the app developer’s control, for example if it is a third party web service. This approach also has issues if the app has to talk to arbitrary servers such as a web browser or email app.
> In order to address these downsides, servers are typically configured with certificates from well known issuers called [Certificate Authorities (CAs)](https://en.wikipedia.org/wiki/Certificate_authority). The host platform generally contains a list of well known CAs that it trusts. As of Android 8.0 (API level 26), Android contained over 100 CAs that are updated in each release and do not change from device to device. Similar to a server, a CA has a certificate and a private key. When issuing a certificate for a server, the CA [signs](https://en.wikipedia.org/wiki/Digital_signature) the server certificate using its private key. The client can then verify that the server has a certificate issued by a CA known to the platform.
> However, while solving some problems, using CAs introduces another. Because the CA issues certificates for many servers, you still need some way to make sure you are talking to the server you want. To address this, the certificate issued by the CA identifies the server either with a specific name such as *gmail.com* or a wildcarded set of hosts such as **.google.com*.
> [https://developer.android.com/training/articles/security-ssl](https://developer.android.com/training/articles/security-ssl)

If we run an Android phone/emulator, and go to **settings / Security / Encryption & credentials / Trusted credentials** (the position might be different on different Android versions and/or manufacturers), you would see lots of certificates that are pre-installed along with the system like.

![](https://cdn-images-1.medium.com/max/2000/1*04_w-ipiNi0ZFcwEo1Sabg.png)

And Google would update the list along with every system upgrade. It means we can trust these certificates and access the website with them.

And on the iOS system, there are some trusted certificates pre-installed and Apple would maintain the list according to

[**Apple Developer Documentation**](https://developer.apple.com/documentation/security/preventing_insecure_network_connections)

## Check the certificate

If we open a website with HTTPS protocol, for instance, wikipedia.org, and click the lock icon at the lefthand of the address bar as

![](https://cdn-images-1.medium.com/max/3376/1*v1L_hzfp7kAhknD00DPTtQ.png)

and click **Certificate**, you would see the details of the certificate for this website as

![](https://cdn-images-1.medium.com/max/4536/1*68nd1w26PSESBJmRFfBMMQ.png)

You would find all details like the CA of this certificate.

You might notice there are 3 levels of certificates, root certificate, intermediate certificate, and leaf certificate (server certificate). And the trusted certificates pre-installed on the device are usually root certificates. It means we could trust any certificates issued by this CA even though this CA might issue multiple intermediate certificates for different websites. So there might be a risk that the root certificate could not be trusted if any intermediate certificate issued by this CA is compromised. This risk includes a Man-in-the-middle attack, we would talk about it in the next chapter.

## Man-in-the-Middle attack

![](https://cdn-images-1.medium.com/max/2800/1*3tsdANIUVuagkbiXOjw4tQ.png)
> In [cryptography](https://en.wikipedia.org/wiki/Cryptography) and [computer security](https://en.wikipedia.org/wiki/Computer_security), a **man-in-the-middle** attack is a [cyberattack](https://en.wikipedia.org/wiki/Cyberattack) where the attacker secretly relays and possibly alters the communications between two parties who believe that they are directly communicating with each other. One example of a MITM attack is active [eavesdropping](https://en.wikipedia.org/wiki/Eavesdropping), in which the attacker makes independent connections with the victims and relays messages between them to make them believe they are talking directly to each other over a private connection, when in fact the entire conversation is controlled by the attacker. The attacker must be able to intercept all relevant messages passing between the two victims and inject new ones.
> As it aims to circumvent mutual authentication, a MITM attack can succeed only when the attacker impersonates each endpoint sufficiently well to satisfy their expectations. Most cryptographic protocols include some form of endpoint authentication specifically to prevent MITM attacks. For example, [TLS](https://en.wikipedia.org/wiki/Transport_Layer_Security) can authenticate one or both parties using a mutually trusted [certificate authority](https://en.wikipedia.org/wiki/Certificate_authority).
> [https://en.wikipedia.org/wiki/Man-in-the-middle_attack](https://en.wikipedia.org/wiki/Man-in-the-middle_attack)

In one scenario, the attacker could compromise a CA and add a fake intermediate certificate under the root certificate issued by this CA, so the system would trust this fake certificate by default. It means if the attacker could intercept the encrypted communication. This is Man-in-the-middle.

## Experiments

Let’s do a simple experiment with [Charles proxy](https://www.charlesproxy.com/). Charle proxy could act certain kind of role of man-in-the-middle, although it needs its own certificate which is not issued by any trusted CA.

The first step is to install Charles proxy as instructed, you might need a license although it allows you have a 30-days free trial.

And then let’s create a simple Android project with an **Empty Activity** template and try to send some HTTPS requests to wikipedia.org and see what would happen.

We need to add

    <uses-permission android:name="android.permission.INTERNET"/>

to the AndroidManifest.xml to allow the app to access the internet.

We also need to update the activity_main.xml to display the webpage in a **WebView** as followed

    *<?*xml version="1.0" encoding="utf-8"*?>
    *<layout>
        <androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
            xmlns:app="http://schemas.android.com/apk/res-auto"
            xmlns:tools="http://schemas.android.com/tools"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            tools:context=".MainActivity">
    
            <WebView
                android:id="@+id/webview"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                app:layout_constraintBottom_toBottomOf="parent"
                app:layout_constraintLeft_toLeftOf="parent"
                app:layout_constraintRight_toRightOf="parent"
                app:layout_constraintTop_toTopOf="parent" />
    
        </androidx.constraintlayout.widget.ConstraintLayout>
    </layout>

and add

    dataBinding{
        enabled = true
    }

to the app-level build.gradle file to enable the data binding.

So we can add a **DataBinding** instance in the MainActivity class and inflate the view in the onCreate() function as

    ActivityMainBinding binding;
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
    //        setContentView(R.layout.activity_main);
            binding = ActivityMainBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());
            binding.webview.getSettings().setJavaScriptEnabled(true);
        }

and add a **LiveData** to the MainActivity class to save the webpage content as

    private MutableLiveData<String> data;
    public MutableLiveData<String> getData() {
        if (data == null) {
            data = new MutableLiveData<String>();
        }
        return data;
    }
    
    final Observer<String> observer = new Observer<String>() {
        @Override
        public void onChanged(@Nullable final String data) {
            binding.webview.loadDataWithBaseURL(null, data, "text/html; charset=utf-8", "UTF-8", null);
        }
    };

We need to wrap the webpage data in a **LiveData** and set an observer is because we need to fetch the webpage in a separate async task and it might take some time.

And then let’s implement an async task to fetch the data from the website as follow

    private static class SendRequest extends AsyncTask<Void,Void,Void> {
            WeakReference<Activity> mWeakActivity;
    
            public SendRequest(Activity activity) {
                mWeakActivity = new WeakReference<Activity>(activity);
            }
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    URL url = new URL("https://wikipedia.org");
                    String response = "";
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    //                conn.setRequestMethod("POST");
                    InputStreamReader in = new InputStreamReader(conn.getInputStream());
                    BufferedReader br = new BufferedReader(in);
                    String line= "";
                    while ((line= br.readLine()) != null) {
                        response += line;
                    }
                    conn.disconnect();
                    Log.d("AAA", response);
                    ((MainActivity)mWeakActivity.get()).data.postValue(response);
                } catch (Exception e) {
                    e.printStackTrace();
                }
    
                return null;
            }
        }

It’s very straightforward that we would send a request to ***https:wikipedia.org***, and set the response (webpage) to the **LiveData**, data.

The only thing that needs to be noticed is we need to create a weak reference to our MainActivity object. This is the recommended way to update UI from a background thread.

We also need to call

    getData().observe(this, observer);
    new SendRequest(this).execute();

in the onCreate() function to set the observer, and to execute the **AsyncTask**.

And then let’s run the app, you would see the Wikipedia webpage is showing.

![](https://cdn-images-1.medium.com/max/2000/1*ir-ZfCw2fA-bdwXdpic1xg.png)

In this article, our main focus is the security communication on Android devices, NOT the detail of the UI, so we won’t invest too much time in UI.

Ok, we have verified we are able to access a website with the trusted certificate by default. And let’s see what would happen if we set up the Charles proxy as MiTM. (How to setup Charles proxy is out of the scope of this article, you could follow the instruction to accomplish it)

You would NOT see the webpage displaying successfully like

![](https://cdn-images-1.medium.com/max/2000/1*r8Fg6Lsl7u1DNjfgzwvHhg.png)

and if you check the Charles proxy, you wound find log like

![](https://cdn-images-1.medium.com/max/5044/1*Wd0gkuit2d2Y_5Kv88-QAQ.png)

The connection failed. And we can also get the stack trace from the logcat as

![](https://cdn-images-1.medium.com/max/7616/1*mbF4SCqFgHaFJMhYL02ToA.png)

It threw a **CertPathValidatorException** because the certificate of Charles proxy is not on the trusted list.

But let’s go back to the original problem: what would happen if someone creates a fake certificate and adds to the trusted list. It’s obvious that the app still could access the website with this fake certificate. That is a MiTM attack.

So to solve this issue, lots of articles suggest adding extra restrictions to the app by trusting some explicit certificates only instead of trusting all pre-installed certificates. That is certificate pinning.

Certificate pinning is a widely used approach to enhance security and there are lots of articles that recommend it like

[https://developer.android.com/training/articles/security-ssl#Pinning](https://developer.android.com/training/articles/security-ssl#Pinning).

## Certificate Pinning

We can implement certificate pinning (Cert pinning) by adding a new **Network Security Configuration** or with **OkHttp CertificatePinner**.

### Network Security Configuration

The first step is to create a network security config file, **network_security_config.xml***,* under **res/xml**

and add

    android:networkSecurityConfig="@xml/network_security_config"

as an attribute of the **application** tag in the AndroidManifest.xml.

and add

    <?xml version="1.0" encoding="utf-8"?>
    <network-security-config>
        <domain-config cleartextTrafficPermitted="true">
            <domain includeSubdomains="true">wikipedia.org</domain>
            <pin-set expiration="2022-01-01">
                <pin digest="SHA-256">ikocd6xWf/wVZnOoeTONMD0v2N8TTr7N1u67gQ+ZxbI=</pin>
            </pin-set>
        </domain-config>
    </network-security-config>

to the **network_security_config.xml**. The hash key is the hashed public key of the certificate for *wikipedia.org*. Be careful that it might be updated or rotated. You could get more information about the certificate for a website from [https://www.ssllabs.com/ssltest/analyze.html](https://www.ssllabs.com/ssltest/analyze.html).

And then if you build and run the app again, the app is still able to access the website. But if someone intercepts the communication between the app and the website, and replies to the app with a fake certificate, the app wound NOT be able to access the website, even though the fake certificate might be issued by the same/different trusted CA. That is certificate pinning.

We can also implement the certificate pinning with

### OkHttp CertificatePinner

[OkHttp](https://square.github.io/okhttp/4.x/okhttp/) is an HTTP+HTTP/2 client for Android and Java applications, which is widely used on the Android platform.

To use the **OkHttp** library, we need to add the following dependencies to the app-level **build.gradle** file

    implementation("com.squareup.okhttp3:okhttp:4.9.1")
    implementation("com.squareup.okhttp3:okhttp-urlconnection:4.9.1")

and implement another async task like

    private static class SendRequestWithOkHttp extends AsyncTask<Void,Void,Void> {
            WeakReference<Activity> mWeakActivity;
    
            public SendRequestWithOkHttp(Activity activity) {
                mWeakActivity = new WeakReference<Activity>(activity);
            }
    
            @Override
            protected Void doInBackground(Void... params) {
                Log.d("AAA", "SendRequestWithOkHttp");
                try {
                    URL url = new URL("https://wikipedia.org");
                    Request request = new Request.Builder()
                            .url(url)
                            .build();
                    String response = "";
                    CertificatePinner certificatePinner = new CertificatePinner.Builder()
                            .add("wikipedia.org", "sha256/ikocd6xWf/wVZnOoeTONMD0v2N8TTr7N1u67gQ+ZxbI=")
                            .build();
                    OkHttpClient okHttpClient = new OkHttpClient.Builder()
                            .certificatePinner(certificatePinner)
                            .build();
                    InputStreamReader in = new InputStreamReader( okHttpClient.newCall(request).execute().body().byteStream());
                    BufferedReader br = new BufferedReader(in);
                    String line= "";
                    while ((line= br.readLine()) != null) {
                        response += line;
                    }
                    Log.d("AAA", response);
                    ((MainActivity)mWeakActivity.get()).data.postValue(response);
                } catch (Exception e) {
                    e.printStackTrace();
                }
    
                return null;
            }
        }

It’s pretty similar to the async task in the previous chapter. But we are able to add a **CertificatePinner** to the **OKHttpClient** instance. We can also pin multiple hostnames with multiple keys in the **CertificatePinner** object.

Then if we can execute this async task in the onCreate() function and remove the network security config file, we should still be able to access the website successfully, and don’t need to worry about the MiTM.

### TrustKit

There are also some third-party libraries to support certificate pinning, like TrustKit
[**GitHub - datatheorem/TrustKit: Easy SSL pinning validation and reporting for iOS, macOS, tvOS and…**](https://github.com/datatheorem/TrustKit)

It supports iOS as well.

### The downside of certificate pinning

It’s obvious that the app would be failed to access the website if the certificate for the website changed because we hardcoded the keys of the trusted certificate in our code in NSC or with CertificatePinner. It means we might need to force update the app if the certificate for the website changed. That is the downside of certificate pinning, and which is not recommended by Google.
> **Caution:** Certificate Pinning is not recommended for Android applications due to the high risk of future server configuration changes, such as changing to another Certificate Authority, rendering the application unable to connect to the server without receiving a client software update.
> [https://developer.android.com/training/articles/security-ssl#Pinning](https://developer.android.com/training/articles/security-ssl#Pinning)

So there are still lots of controversies regarding the certificate pinning, we might need to analyze the scenario case by case.

## debug-overrides

The last topic of this article is another section in the Network Security Config, the **<debug-overrides>**.

If you remember, we were trying to capture the traffic with Charles proxy, which is pretty helpful during the development and debug. But It would fail by default because the certificate of Charles proxy is not on the trusted certificate list.

We can override it by adding

    <debug-overrides>
        <trust-anchors>
            <!-- Trust user added CAs while debuggable only -->
            <certificates src="user" />
        </trust-anchors>
    </debug-overrides>

to the NSC file. This config would allow you to trust user-added CAs on debug build.

After we added this section to the NSC file, we can build and run the Charles proxy and launch the app again, you would see the Charles proxy could capture the traffic as

![](https://cdn-images-1.medium.com/max/4588/1*5lQ8_0tUddwlJERROBjslA.png)

This is a pretty convenient function for developers to debug the mobile app.

That is all for this article regarding secure communication, certificate Man-in-the-middle, and certificate pinning. Thanks for reading.
