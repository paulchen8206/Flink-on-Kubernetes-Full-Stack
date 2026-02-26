import time
from kafka import KafkaAdminClient
import os

broker = os.environ.get('KAFKA_BROKER', 'kafka:9092')

while True:
    try:
        admin = KafkaAdminClient(bootstrap_servers=broker)
        admin.close()
        print(f"Kafka broker {broker} is available.")
        break
    except Exception as e:
        print(f"Waiting for Kafka broker {broker}... {e}")
        time.sleep(5)
