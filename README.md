# 🚌 N-Furgón – School Transport Tracking Platform

> Real-time monitoring and management platform for school transportation in Chile.  
> Multi-module Android app + Web Admin Panel — built with Kotlin and Firebase.

[![Driver App](https://img.shields.io/badge/GitHub-Driver%20App-181717?logo=github)](https://github.com/Byron1235/nfurgonapp)
[![Tutor App](https://img.shields.io/badge/GitHub-Tutor%20App-181717?logo=github)](https://github.com/Jose073/nfurgonTutor)
[![Status](https://img.shields.io/badge/Status-Completed%20%7C%20Nov%202024-blue)]()

---

## 📌 Overview

**N-Furgón** is a multi-platform solution that connects parents, drivers, and school administrators in real time for the monitoring and management of school transport vans (*furgones escolares*) in Chile.

The system is split into three independent modules, each serving a distinct actor:

| Module | Platform | Repository |
|---|---|---|
| 🚗 Driver App | Android (Kotlin) | [Byron1235/nfurgonapp](https://github.com/Byron1235/nfurgonapp) |
| 👨‍👧 Tutor/Parent App | Android (Kotlin) | [Jose073/nfurgonTutor](https://github.com/Jose073/nfurgonTutor) |
| 🖥️ Admin Panel | Web (deployed) | [Live →](https://nfurgonapp.web.app/) |

---

## 🎯 Key Features

- 📍 **Real-time GPS tracking** — live vehicle location via Google Maps API
- 🔔 **Automatic push notifications** — arrival alerts via Firebase Cloud Messaging (FCM)
- 💬 **Parent–driver communication** — in-app direct messaging
- 📋 **Incident reporting** — structured logging for drivers and admins
- 🛡️ **Role-based access** — separate interfaces for parents, drivers, and administrators
- ☁️ **Real-time cloud sync** — instant data across all devices via Firebase Realtime Database
- 🔗 **RENASTRE integration** — compatibility with Chile's national school transport registry

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Mobile (Driver & Tutor) | Kotlin · Android Studio |
| Real-time Database | Firebase Realtime Database |
| Push Notifications | Firebase Cloud Messaging (FCM) |
| Authentication | Firebase Auth |
| Maps & Geolocation | Google Maps API |
| Web Admin Panel | Firebase Hosting |
| Cloud Infrastructure | Google Cloud (via Firebase) |
| Methodology | Scrum · IEEE 830 |

---

## 🏗️ Architecture

The platform follows a **modular architecture** with three independent clients sharing a common Firebase backend:

```
┌─────────────────────────────────────────────┐
│              Firebase Backend               │
│  Realtime DB · Auth · FCM · Cloud Storage   │
└────────────┬─────────────┬─────────────────┘
             │             │             │
    ┌────────▼──┐  ┌───────▼──┐  ┌──────▼──────┐
    │ Driver App│  │ Tutor App│  │ Admin Panel │
    │  (Kotlin) │  │ (Kotlin) │  │    (Web)    │
    └───────────┘  └──────────┘  └─────────────┘
             │             │             │
    ┌────────▼─────────────▼─────────────▼───┐
    │           External Integrations         │
    │   Google Maps API · RENASTRE · FCM      │
    └─────────────────────────────────────────┘
```

**Module breakdown:**
- **Driver App** — GPS broadcast, route management, incident reporting, student boarding status
- **Tutor/Parent App** — real-time tracking view, push notifications, messaging with driver
- **Admin Panel (Web)** — fleet management, user administration, reporting dashboard

---

## 🚀 Deployment

The platform was fully deployed and operational during the academic demonstration period (Nov 2024):

- 🌐 **Web Admin Panel** — hosted on Firebase Hosting
- 📱 **Driver & Tutor apps** — tested on physical Android devices
- ☁️ **Backend** — Firebase Realtime Database + FCM running on Google Cloud

> Services have since been decommissioned. Screenshots and architecture diagrams are available in `/docs`.

---

## 📐 Development Methodology

- **Scrum** — iterative sprints with backlog, user stories, and sprint reviews
- **Requirements documented** under IEEE 830 standard
- **RACI matrix** for team responsibility assignment
- **Tech selection** — evaluated Kotlin vs Flutter vs React Native; chose Kotlin for native Android performance and Google ecosystem integration

---

## 👥 Team

| Name | Role | GitHub |
|---|---|---|
| Byron Troncoso Ortega | Developer | [@Byron1235](https://github.com/Byron1235) |
| Álvaro Navarrete Ibáñez | Developer | — |
| José Navarrete Ibáñez | Developer | [@Jose073](https://github.com/Jose073) |

**Academic context:** Proyecto de Título — Ingeniería en Informática  
**Advisor:** Christopher Muñoz Parra · **Date:** November 2024

---

## 📄 Documentation

Full project formulation document (Spanish) available in `/docs`:
- Problem identification & market research
- Requirements under IEEE 830 standard
- Tech stack comparative analysis
- Architecture diagrams
- Financial feasibility (NPV, IRR)
- KPIs & SLA definitions
- Gantt / project timeline

---

## 📫 Contact

**Byron Troncoso Ortega**  
[![LinkedIn](https://img.shields.io/badge/LinkedIn-Connect-blue?logo=linkedin)](https://www.linkedin.com/in/tu-perfil)
[![GitHub](https://img.shields.io/badge/GitHub-Byron1235-black?logo=github)](https://github.com/Byron1235)
