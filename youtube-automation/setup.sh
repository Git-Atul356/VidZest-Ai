#!/bin/bash

# YouTube Automation Interactive Keys Configuration Setup Tool

echo "=================================================="
echo " 🛠️   TubeAutomate Headless Server Setup Tool       "
echo "=================================================="
echo "This helper tool dynamically configures your environment variables"
echo "to run the 24/7 AI-Agent YouTube channel manager."
echo ""

# Active provider
read -p "Select AI Provider (Gemini / OpenAI) [Default: Gemini]: " PROVIDER
PROVIDER=${PROVIDER:-Gemini}

# API Keys
if [ "$PROVIDER" = "OpenAI" ] || [ "$PROVIDER" = "openai" ]; then
    read -sp "Enter OpenAI API Key: " OPENAI_KEY
    echo ""
    GEMINI_KEY=""
else
    read -sp "Enter Google Gemini API Key: " GEMINI_KEY
    echo ""
    OPENAI_KEY=""
fi

# Channel Niche
read -p "Enter Target Channel Niche [Default: Tech Gossip]: " NICHE
NICHE=${NICHE:-"Tech Gossip"}

# Save to .env
cat <<EOF > .env
# --- TubeAutomate Configuration ---
API_PROVIDER=$PROVIDER
TARGET_NICHE=$NICHE
GEMINI_API_KEY=$GEMINI_KEY
OPENAI_API_KEY=$OPENAI_KEY
EOF

echo ""
echo "✔️   Configured .env file successfully!"
echo "You can now run 'docker build -t yt-automate .' or 'npm start' to start the service!"
echo "=================================================="
