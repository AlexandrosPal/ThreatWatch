# ThreatWatch

## Introduction
**ThreatWatch** is a lightweight, self-hosted CVE monitoring solution designed for developers and teams to help them stay informed about vulnerabilities affecting the technologies they use. 

It delivers alerts from through multiple channels, like email, Discord, or Slack. It offers extensive customization when it comes to the products it monitors, polling intervals and control how CVEs are filtered, tracked and reported.

## Features
- CVE monitoring from NVD
- Early vulnerability detection before full NVD enrichment
- CPE-based component tracking
- Discord / Slack / Teams / Email notifications
- Severity filtering
- Deduplication
- Self-hosted with Docker Compose
- Lightweight and AI-free by default

## Architecture Overview
ThreatWatch consists of three main components:
- **Frontend**: React-based UI for configuring monitoring settings.
- **Backend**: Spring Boot API responsible for scheduling, CVE retrieval, filtering, deduplication, and notifications.
- **Redis**: Stores user settings and CVE state.

## Getting Started

### Prerequisites
- Docker
- Docker Compose
- Optional: NVD API key

### Entrypoints
- Frontend settings page -> localhost:5173
- Backend APIs -> localhost:8080

### Notifications Supported
- Email
- Discord
- Slack
- MS Teams

### Run with Docker Compose

```bash
git clone https://github.com/AlexandrosPal/ThreatWatch.git
cd infra/
docker compose up --build
```

If run on a VM, one way to open settings page is via port-forwarding:
```bash
ssh -i path_to_key \
    -L 5173:vm_private_ip:5173 \
    -L 8080:vm_private_ip:8080 \
    user@vm_public_ip
```