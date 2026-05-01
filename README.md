# ThreatWatch

## Introduction
**ThreatWatch** is a lightweight, self-hosted CVE monitoring solution designed for developers and teams to help them stay informed about vulnerabilities affecting the technologies they use. It delivers alerts from through multiple channels, like email, Discord, or Slack. It offers extensive customization when it comes to the products it monitors, polling intervals and control how CVEs are filtered, tracked and reported.

## Features
- Self-hosted CVE monitoring
- NVD API integration
- Configurable watchlist of technologies
- Severity threshold filtering
- Early alerts for CVEs without CVSS scores
- Email provider configuration
- Optional NVD API key support for higher request throughput
- Manual scan execution

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

### Run with Docker Compose

```bash
cd .\infra\
docker compose up --build