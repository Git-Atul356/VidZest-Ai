# TubeAutomate Server

TubeAutomate is a headless 24/7 self-driving YouTube channel backend manager. It utilizes multiple AI agents coordinating to research trending niches, write narratives scripts, generate responsive assets, and schedule automatic publishing.

## Features

- **Topic Researcher Agent**: Scans Google Trends / API scopes to isolates 5 click-worthy niches.
- **Narrative Script Writer Agent**: Generates custom 3-minute narration sequences with Audio & Visual cues.
- **SEO Title & Tag Optimizer**: Formats optimal high-CTR metadata.
- **SQLite Database Persistence**: Stores comprehensive execution logs and scheduling calendars in real time.
- **Dual AI Engines**: Toggle between Google Gemini and OpenAI.

## Deployment Guide

### Option 1: Standard Node.js Setup

1. Configure variables interactively:
   ```bash
   chmod +x setup.sh
   ./setup.sh
   ```
2. Build and launch:
   ```bash
   npm install
   npm start
   ```

### Option 2: Docker Stack Setup

To run persistently in the background on your server:

```bash
docker build -t yt-automate .
docker run -d \
  --name yt-channel-bot \
  -v $(pwd)/database:/app/database \
  --env-file .env \
  yt-automate
```

Enjoy driving your automated YouTube channels autonomously!
