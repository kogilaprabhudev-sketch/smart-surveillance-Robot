"""
Raspberry Pi client for the Spring Boot backend.

Replaces the Telegram/GPIO logic of Driver_app.py:
  * every 0.5 s  -> reads ultrasonic / IR / sound sensors and POSTs them
  * every 1 s    -> polls the server for operator motor commands
  * send_detection() -> call this from your YOLO loop when something is detected

The Telegram token is NOT needed on the Pi any more - the server sends alerts.

Install:   pip install requests
Run:       SERVER_URL=http://<server-ip>:8080 ROBOT_API_KEY=... python3 robot_client.py
"""
import os
import random
import threading
import time

import requests
import RPi.GPIO as GPIO

SERVER_URL = os.environ.get("SERVER_URL", "http://localhost:8080")
ROBOT_CODE = os.environ.get("ROBOT_CODE", "SPSR-01")
API_KEY = os.environ["ROBOT_API_KEY"]          # must match the server's ROBOT_API_KEY
BASE = f"{SERVER_URL}/api/robot/{ROBOT_CODE}"
HEADERS = {"X-API-KEY": API_KEY}

# --- pins (same as the project report) ---
IR_SENSOR, SOUND_SENSOR = 21, 20
TRIG, ECHO = 23, 18
M1_0, M1_1, M2_0, M2_1 = 6, 13, 19, 26

GPIO.setwarnings(False)
GPIO.setmode(GPIO.BCM)
GPIO.setup(IR_SENSOR, GPIO.IN, pull_up_down=GPIO.PUD_UP)
GPIO.setup(SOUND_SENSOR, GPIO.IN, pull_up_down=GPIO.PUD_UP)
GPIO.setup(TRIG, GPIO.OUT)
GPIO.setup(ECHO, GPIO.IN)
for pin in (M1_0, M1_1, M2_0, M2_1):
    GPIO.setup(pin, GPIO.OUT)
GPIO.output(TRIG, False)
time.sleep(2)

# --- motors ---
def _drive(a, b, c, d):
    GPIO.output(M1_0, a); GPIO.output(M1_1, b)
    GPIO.output(M2_0, c); GPIO.output(M2_1, d)

H, L = GPIO.HIGH, GPIO.LOW
MOVES = {
    "FORWARD": lambda: _drive(H, L, H, L),
    "REVERSE": lambda: _drive(L, H, L, H),
    "LEFT":    lambda: _drive(H, L, L, L),
    "RIGHT":   lambda: _drive(L, L, H, L),
    "STOP":    lambda: _drive(L, L, L, L),
}
manual_until = 0.0     # while > now, operator commands override random patrol


def get_distance():
    GPIO.output(TRIG, True); time.sleep(0.00001); GPIO.output(TRIG, False)
    t = time.time()
    while GPIO.input(ECHO) == 0:
        if time.time() - t > 0.02:
            return None
    start = time.time(); t = time.time()
    while GPIO.input(ECHO) == 1:
        if time.time() - t > 0.02:
            return None
    return round((time.time() - start) * 34300 / 2, 2)


def post(path, payload):
    try:
        requests.post(f"{BASE}/{path}", json=payload, headers=HEADERS, timeout=5)
    except requests.RequestException as e:
        print("server unreachable:", e)


def send_detection(object_class, confidence, lat=None, lon=None):
    """Call from the YOLO code, e.g. send_detection('person', 0.87, lat, lon)."""
    post("detections", {"objectClass": object_class, "confidence": float(confidence),
                        "latitude": lat, "longitude": lon})


def sensor_loop():
    while True:
        post("sensors", {"ultrasonicCm": get_distance(),
                         "irTriggered": GPIO.input(IR_SENSOR) == 0,
                         "soundTriggered": GPIO.input(SOUND_SENSOR) == 0})
        time.sleep(0.5)


def command_loop():
    global manual_until
    while True:
        try:
            r = requests.get(f"{BASE}/commands/next", headers=HEADERS, timeout=5)
            if r.status_code == 200:
                cmd = r.json()["command"]
                print("command:", cmd)
                MOVES[cmd]()
                # STOP keeps the robot still for 5 s; other moves run for ~1 s
                manual_until = time.time() + (5.0 if cmd == "STOP" else 1.0)
        except requests.RequestException as e:
            print("server unreachable:", e)
        time.sleep(1)


if __name__ == "__main__":
    threading.Thread(target=sensor_loop, daemon=True).start()
    threading.Thread(target=command_loop, daemon=True).start()
    try:
        while True:                                   # autonomous patrol (as in Driver_app.py)
            if time.time() < manual_until:
                time.sleep(0.1)
                continue
            if GPIO.input(IR_SENSOR) == 0 or GPIO.input(SOUND_SENSOR) == 0:
                random.choice([MOVES["FORWARD"], MOVES["REVERSE"], MOVES["LEFT"], MOVES["RIGHT"]])()
                time.sleep(random.uniform(0.3, 1.0))
                MOVES["STOP"]()
                time.sleep(0.5)
            else:
                MOVES["STOP"]()
                time.sleep(0.1)
    except KeyboardInterrupt:
        MOVES["STOP"]()
        GPIO.cleanup()
