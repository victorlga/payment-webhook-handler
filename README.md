# payment-webhook-handler

Here’s a clean and concise `README.md` for your project:

---

# 🧾 Payment Webhook Handler

A Clojure webhook handler for processing and validating payment events with SQLite persistence and HTTP callbacks for confirmation/cancellation.

## 📦 Features

* Validates a secure token from incoming webhooks
* Checks for duplicate or malformed payloads
* Confirms or cancels transactions via internal HTTP APIs
* Persists transaction IDs in an SQLite database
* Supports both HTTP (port 5000) and HTTPS (port 5443)

---

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/victorlga/payment-webhook-handler.git
cd payment-webhook-handler
```

### 2. Build Docker Image

Make sure Docker is running:

```bash
docker build -t clojure-webhook-handler -f .devcontainer/Dockerfile .
```

### 3. Run the Server

If ports `5000` (HTTP) or `5443` (HTTPS) are in use, **change the exposed ports** (`sink port` → *you probably meant `host port`*).

```bash
docker run -p 5000:5000 -p 5443:5443 clojure-webhook-handler
```

You should see something like:

```
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
...
```

---

## 🧪 Testing the Webhook

### 1. Navigate to the Python Test Script

```bash
cd python
```

### 2. Set Up Python Environment

```bash
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt
```

### 3. Run the Test

```bash
python3 test_webhook.py
```

> ⚠️ If you changed the port during Docker run, make sure to update it in `test_webhook.py`.

---

## 🔐 Testing HTTPS (WIP)

> Documentation for HTTPS testing is in progress. Ensure your `keystore.p12` and password (`changeit`) are valid and configured properly.

```bash
keytool -genkeypair -alias server-key \
  -keyalg RSA -keysize 2048 -storetype PKCS12 \
  -keystore keystore.p12 -validity 365 \
  -storepass changeit -keypass changeit \
  -dname "CN=localhost, OU=Dev, O=MyCompany, L=City, S=State, C=BR"
```

---

## 📁 Project Structure

```
.
├── core.clj              ; Main handler logic
├── project.clj           ; Leiningen config
├── .devcontainer/
│   └── Dockerfile        ; Docker setup
├── data/                 ; SQLite database
└── python/
    ├── test_webhook.py   ; Test client for webhook
    └── requirements.txt  ; Python dependencies
```

---

## 🛡️ License

Copyright © 2025 FIXME

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
