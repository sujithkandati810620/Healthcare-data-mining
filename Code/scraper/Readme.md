
# Scraper

This Python-based web scraper is designed to extract valuable content from healthcare discussion forums. The scraper currently supports scraping the following websites:

1. patient.info
2. medhelp.org
3. mayoclinic.org
4. livescience.com

The purpose of this tool is to gather information from these healthcare discussion forums, enabling the extraction of relevant data for analysis.

## Installation

Before using the scraper, make sure you have the following installed:

Python version 3.8 or higher
Scrapy library

You can install Scrapy using the following command: pip install scrapy

## Using scraper

1. Clone the repository to your local machine.
2. Navigate to the scraper directory.
3. Inside the scraper folder, you will find a spiders directory. This directory contains individual spiders for each supported website.
4. To run a specific spider, use the following command in the terminal: scrapy crawl spider_name
5. Replace spider_name with the name of the spider you want to run.
6. Output file with the scraped data will be generated in the data folder.
