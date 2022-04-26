#!/bin/bash
#inspired  from : https://github.com/grpc/grpc-java/tree/master/examples#generating-self-signed-certificates-for-use-with-grpc

#output files
#ca.key: Certificate Authority private key file (this shouldn't be shared on real life)
#ca.crt: Certificate Authority trust certificate (this should be shared with users in real-life)
#server.key: Server private key, password protected (this shouldn't be shared)
#server.csr: Server certificate using signing request(this should be shared with the CA owner)
#server.crt: Server certificate signed by the CA (this would be sent back to the CA owner) - keep on server
#server.perm: Conversion of server.key into a format gRPC likes (this shouldn't be shared)

#Summary
#Private files: ca.key, server.key, server.pem, server.crt
#"Share" files: ca.crt (needed by the client) , server.csr (needed by the CA)

# Changes these CN's to match your hosts in your environment if needed
SERVER_CN=localhost

# Step 1: Generate Certificate Authority + Trust Certificate (ca.crt)
openssl genrsa -passout pass:1111 -des3 -out ca.key 4096
openssl req -passin pass:1111 -new -x509 -days 365 ca.key -out ca.crt -subj "/CN=${SERVER_CN}"

# Step 2: Generate the Server Private key (server.key)
openssl genrsa -passout pass:1111 -des3 -out server.key 4096

# Step 3: Get a signed certificate  from the CA (server.csr)
openssl genrsa -passout pass:1111 -new -key server.key -out server.csr -subj "/CN=${SERVER_CN}"

# Step 4: Sign the certificate with the CA we created ( it's called  self signing0 -server.crt
openssl x509 -req -passin pass:1111 -days 365 -in server.csr -CA ca.crt  -CAkey ca.key -set_serial 01 -out server.crt

#Step 5: Convert  the server certificate  to .pem format (server.pem) - usable by gRPC
openssl pkcs8 -topk8 -nocrypt -passin pass:1111 -in server.key -out server.pem
