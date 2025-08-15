import requests
import json
import logging
import random
import time
import uuid
from datetime import datetime, timezone

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

EVENT_TYPES = ["click", "view", "scroll", "purchase"]
PAGES = ["/", "/products", "/products/123", "/cart", "/checkout", "/search?q=shoes"]
ELEMENT_IDS = ["btn-buy", "lnk-more", "img-hero", "fld-search", "btn-add-cart"]

# Using the priv-IP of my Windows in-place of localhost because I use this script from a WSL terminal.
API_URL = "http://10.0.0.117:8080/api/track"  # your Spring REST endpoint
ENDPOINTS = ["click", "view", "scroll", "event"]
SESSION_IDS = [str(uuid.uuid4()) for _ in range(200)]  # reuse sessions to feel realistic

USE_EPOCH_MILLIS = True  # set False to send ISO-8601 strings

def now_epoch_millis() -> int:
    return int(datetime.now(timezone.utc).timestamp() * 1000)

def now_iso8601_z() -> str:
    # Millisecond precision, explicit Z suffix
    return datetime.now(timezone.utc).isoformat(timespec="milliseconds").replace("+00:00", "Z")

def random_ipv4():
    return ".".join(str(random.randint(0, 255)) for _ in range(4))

def is_prime(n):
    if n < 2:
        return False
    for i in range(2, int(n**0.5) + 1):
        if n % i == 0:
            return False

    return True

def make_event(prime_check):
    user_id = random.randint(1, 5000)
    event_type = random.choice(EVENT_TYPES)
    page = random.choice(PAGES)
    element_id = random.choice(ELEMENT_IDS)
    session_id = random.choice(SESSION_IDS)
    timestamp = now_epoch_millis() if USE_EPOCH_MILLIS else now_iso8601_z()

    # Using prime numbers to correspond to bad data. If the number is a prime, then the data will purposefully-
    # be made into an unacceptable state that will cause the message to be sent to the user-activity-dlq in Kafka.
    user_id = str(user_id) if not is_prime(prime_check) else user_id
    print(type(user_id))
    return {
        "userId": user_id,
        "eventType": event_type,
        "page": page,
        "elementId": element_id,
        "sessionId": session_id,
        "ipAddress": random_ipv4(),
        # sprinkle in extra metadata
        "meta": {
            "referrer": random.choice(["direct", "email", "ad", "search"]),
            "abBucket": random.choice(["A", "B"]),
            "viewport": {"w": random.choice([360, 768, 1280, 1440]), "h": random.choice([640, 800, 900])}
        }
    }

def send_event(evt):
    try:
        r = requests.post(API_URL + "/" + random.choice(ENDPOINTS), json=evt, timeout=5)
        r.raise_for_status()
    except requests.RequestException as e:
        logger.error("Failed to send event: %s", e)

if __name__ == "__main__":
    try:
        count = last_count = 0

        while True:
            prime_check = random.randint(1, 100)
            if count > last_count + 50:
                logger.info(f"Sent {count} events to Kafka")
                last_count = count

            evt = make_event(prime_check)
            send_event(evt)
            time.sleep(random.uniform(0.1, 0.6))  # small jitter
            count += 1
    except KeyboardInterrupt:
        logger.info("Stopped.")