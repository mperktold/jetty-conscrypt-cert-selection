# jetty-conscrypt-cert-selection
Example project to show the problem described in [Jetty issue 2896](https://github.com/eclipse/jetty.project/issues/2896).

This is a minimal example implementing a server that has two wildcard certificates `*.alfa.org` and `*.beta.org`.
The problem is that regardless of whether the server always serves the same certificate, regardless of the host specified by the client.

To reproduce the problem, first add the following content to your `hosts` (`/etc/hosts` in Unix, `C:\WINDOWS\System32\drivers\etc` in Windows).

```
127.0.0.1		test.alfa.org
127.0.0.1		test.beta.org
```

Then run `VirtualHosts.main()` and visit both `test.alfa.org` and `test.beta.org` in your browser.
If you view the certificate, you can see that both use `*.alpha.org`.

However, this only happens when using Conscrypt.
To try without, uncomment or remove the code [where the Conscrypt SSL provider is set](https://github.com/mperktold/jetty-conscrypt-cert-selection/blob/a2cdf6ddb6714a8d03946846ce879602911de257/src/main/java/VirtualHosts.java#L78-L79).
