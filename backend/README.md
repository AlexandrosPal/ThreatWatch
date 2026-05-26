# ThreatWatch Backend

ThreatWatch is a self-hosted vulnerability monitoring platform designed to continuously monitor newly published CVEs and notify users when monitored technologies may be affected.

This repository contains the Spring Boot backend responsible for:
- CVE ingestion and processing
- Product matching
- Scheduled scans
- Notification dispatching
- Execution history persistence
- Redis persistence
- REST API exposure

---

# Features

- NVD CVE monitoring
- Scheduled background scans
- Product watchlists
- Severity threshold filtering
- CVE deduplication windows
- Multi-channel notifications
- Execution history tracking
- Dockerized deployment
- REST API for frontend integration

---

# Supported Notification Channels

- Email (SMTP)
- Discord webhooks
- Slack webhooks
- Microsoft Teams webhooks

---

# Tech Stack

- Java 21
- Spring Boot
- Redis
- Docker
- Maven

---

# Architecture

The backend is structured around a scheduled vulnerability monitoring pipeline:

```text
Scheduler
   ↓
NVD CVE Retrieval
   ↓
Matching Engine
   ↓
Deduplication
   ↓
Notification Dispatch
   ↓
Execution Persistence