## Introduction

The MetaMap Annotator is a Java-based application designed to annotate web data using the MetaMap annotator Java API. It efficiently processes data scraped from various sources and prepares it for further analysis. This guide will walk you through the setup and usage of the MetaMap Annotator.

## Prerequisites

Java JDK
Internet Connection for downloading necessary files

## Installation

Step 1: Download Required Software
Download the MetaMap main release from MetaMap Main Download.
Download the MetaMap Java API from MetaMap Java API Download.

Step 2: Setup MetaMap
Extract both downloads.
Merge the public_mm folder from the Java API into the public_mm folder of the main release. Ignore duplicate files during the merge.
Navigate to the public_mm directory and run the installation script:

cd public_mm/
./bin/install.sh

Verify the successful installation by checking the install.log for the creation of mmserver.

Step 3: Start Servers
Start the WSD, MedPost, and MM servers in the following order:

./bin/wsdserverctl start
./bin/skrmedpostctl start
./bin/mmserver

Step 4: Clone and Setup MetaMap Annotator
Clone the MetaMap Annotator project.
Navigate to the MetaMapAnnotator directory.
Launch the application:

java -jar dist/MetaMapAnnotator.jar

## Usage

The application automatically processes CSV files located in the neighboring scraper/data folder.
Ensure that the files do not reside in subfolders.
The annotated output will be written to the resources folder within the MetaMapAnnotator directory.
Run the combine.py script in the resources folder to merge the annotated data from various sources into a single CSV file.
Known Issues
Non-ASCII Characters Crash: If mmserver crashes due to non-ASCII characters in a CSV file, remove the problematic entry and rerun the application.
Additional Notes
The project's main class file contains a list of options used for MetaMap, designed for efficiency and accuracy.
The IgnoredWords.csv and IncludePOSTags.csv files in the resources folder list the ignored words and POS tags used for filtering concepts.
Detailed printing and filtering based on POS tags are disabled for faster processing.
