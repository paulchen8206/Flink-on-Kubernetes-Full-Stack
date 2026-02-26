# salesgen: Synthetic Event Generator

This module generates synthetic purchase events for Kafka, used to test the Flink streaming pipeline.

## Usage
- salesgen is started automatically in the local Docker Compose stack.
- To run manually:
  ```sh
  python purchases.py
  ```
- Configuration: see `configuration/configuration.ini` and `config/kafka.py`.

## Customization
- Edit `models/purchase.py` and `purchases.py` to change event schema or logic.
- Update `data/products.csv` for product catalog.

## References
- [Project Root README](../README.md)

