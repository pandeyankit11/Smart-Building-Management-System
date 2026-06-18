#!/usr/bin/env bash
set -euo pipefail

# ------------------------------------------------------------
# Clean previous persisted data
# ------------------------------------------------------------
rm -rf "OOPS Project/data"
mkdir -p "OOPS Project/data"

# ------------------------------------------------------------
# Run the interactive CLI with a pre‑canned sequence
# ------------------------------------------------------------
cat <<'EOF' | ./OOPS\ Project/run.sh
1                # Login
admin
admin123

3                # Building & Equipment
3                # Add floor
New West Wing
4

6                # Add room on floor 4
4
Conference Room
North Wing
20

9                # Add equipment to the new room
2
Projector
AV
150

4                # Occupancy menu
2                # Set occupancy
2                # Choose Conference Room
25               # Spike occupancy → triggers InvalidInputException

7                # Alerts menu
3                # Add alert
FIRE
Smoke detected in Conference Room
CRITICAL

9                # View Complete Status (optional sanity check)
10               # Save Data (persist state)

# Show the log file before exiting
echo
echo "=== EVENT LOG (last 8 lines) ==="
tail -n 8 "OOPS Project/data/events.log"

12               # Exit
EOF