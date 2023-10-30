# ENSTOEDataRetrieval
A java library to automatically retrieve data from [ENTSOE transparency platform](https://transparency.entsoe.eu/). This project contains everything to automatically retrieve data from transparency platform, and save it as a CSV file. It may be possible that the module is unable to retrieve a part of countries/data. \
This repository is not related in any ways with ENTSOE or transparency platform maintainers. \
This project has been donne in the context of [AI4CZC](https://www.ai4europe.eu/business-and-industry/case-studies/ai4czc), a winner project of [I-Nergy second open call](https://i-nergy.eu/2nd-open-calls-winning-projects). \
**PLEASE, KEEP IN MIND THAT NOT ALL ENTSOE TRANSPARENCY PLATFORM DATA IS FREE FOR REUSE**. \
You can find the list of free to reuse data [here](https://transparency.entsoe.eu/content/static_content/download?path=/Static%20content/terms%20and%20conditions/191025_List_of_Data_available_for_reuse_v2_cln.pdf&loggedUserIsPrivileged=false). Otherwise, you will need to seek the approval of the data owner.

## Build Guide
To build this project you will need : 
1. A JDK, of version at least 17.
2. [Maven](https://maven.apache.org/) 

Simply go to the project and run on the project folder : 
```
mvn install
```
Maven will build a fat jar on the project folder, named "data-retriever.jar".

## Run Guide

To run the project first you will need an ENTSOE transparency access token. For that you will need to register on the platform and then check on the [Transparency Platform RESTful API - user guide](https://transparency.entsoe.eu/content/static_content/Static%20content/web%20api/Guide.html) for the instructions.

Once you got your token, you can run (with a JDK 17 or bigger) the cli : 
```
java -jar data-retriever.jar -t [your_token] -s [start_date] -e [end_date] 
```

### CLI exampls

Here are some CLI examples :

- Retrieve all data from all countries of 2019 to 1st june 2023 to /home/user/Desktop/elec_2019_2023.csv :
```
java -jar data-retriever.jar -t your_token -s 2019-01-01T00:00:00 -e 2023-06-01T00:00:00 -o /home/user/Desktop/elec_2019_2023.csv --all -w --cache

```

- Retrieve generation data for all countries from 2019 to 1st june 2023 to /home/user/Desktop/gen_2019_2023.csv : 
```
java -jar data-retriever.jar -t your_token -s 2019-01-01T00:00:00 -e 2023-06-01T00:00:00 -o /home/user/Desktop/gen_2019_2022.csv -w -cache -c '16.1.B&C' -a "ME,IT-CS,BA,RS,XK"

```

- Retrieve physical flows between Montenegro and all his neighbouring countries between  01/01/2019 and 1st june 2023 to /home/user/Desktop/flows_2019_2023.csv : 
```
 java -jar data-retriever.jar -t your_token -s 2019-01-01T00:00:00 -e 2023-06-01T00:00:00 -o /home/user/Desktop/flows_2019_2023.csv -w -cache -c "12.1.G" -a "IT-CS,BA,RS,XK,AL" -area-flow ME
```

- Retrieve transmission outages between Montenegro and all his neighbouring countries between 01/01/2019 and 1st june 2023 to /home/user/Desktop/outages_grid_2019_2023.csv :
```
 java -jar data-retriever.jar -t your_token -s 2019-01-01T00:00:00 -e 2023-06-01T00:00:00 -o /home/user/Desktop/outages_grid_2019_2023.csv -w -cache -c "10.1.A&B" -a "IT-CS,BA,RS,XK,AL" -area-flow ME
```

- Retrieve generation outages in Montenegro and his neighbouring countries between 01/01/2019 and 1st june 2023 to /home/user/Desktop/outages_generation_2019_2023.csv. The retrieval will be done making queries of 5 Days :
```
 java -jar data-retriever.jar -t your_token -s 2019-01-01T00:00:00 -e 2023-06-01T00:00:00 -o /home/user/Desktop/outages_generation_2019_2023.csv -w -cache -c "15.1.A&B" -a "ME,IT-CS,BA,RS,XK,AL" -max-request-duration P5D
```

- Retrieve net position for Montenegro and all his neighbours between 01/01/2019 and 1st june 2023 to /home/user/Desktop/net_position_forecast_2019_2023.csv :
```
 java -jar data-retriever.jar -t your_token -s 2019-01-01T00:00:00 -e 2023-06-01T00:00:00 -o /home/user/Desktop/net_position_forecast_2019_2023.csv -w -cache -c "11.1.A.C" -a "IT-CS,BA,RS,XK,AL" -area-flow ME
```

### Functionalities

The library is only limited to Montenegro, Serbia, Italy Center-South, Bosnia and Kosovo areas. 

For a more complete description of the CLI commands you can run : 
```
java -jar data-retriever.jar --help
```

## Architecture overview

This project retrieves the parameter from CLI, then makes the queries to transparency platform and reads the provided xml files with JAXB, to create java objects. Then dependending of the type, this are read by the CSVTransformers to generate the target CSVFile.

This project contains these packages : 

- **tech.inceptive.ai4czc.entsoedataretrieval** : All the classes needed to handle the CLI (MainCLI) and the logic of the library when used on CLI.
- **tech.inceptive.ai4czc.entsoedataretrieval.csv** : All the classes to take the intermediate data structures and generate a csv file.
- **tech.inceptive.ai4czc.entsoedataretrieval.exceptions** : Some exceptions.
- **tech.inceptive.ai4czc.entsoedataretrieval.fetcher** : The classes to retrieve data from transparency platform.
- **tech.inceptive.ai4czc.entsoedataretrieval.input** : The input parameters for the fetcher (only datastructures).
- **tech.inceptive.ai4czc.entsoedataretrieval.xjc** : The datastrures to deserialize the provided content from transparency platform.
- **tech.inceptive.ai4czc.entsoedataretrieval.tools** : Some utils classes. 


## Integration on your library
If you want to use all ENTSOEDataretriever functionalities (fetching + csv writing), the best option is to use directly the CLI interface, even programatically. \
But if you only want the fetching functionalities, then the best option is to use the [ENTSOEDataFetcher](https://github.com/inceptive-tech/ENSTOEDataRetrieval/tree/master/src/main/java/tech/inceptive/ai4czc/entsoedataretrieval/fetcher) class, as you will get the data on a proper data structure, and then do your process. 


## About this project

This library was developed by [Inceptive](https://inceptive.tech/) under the [AI4CZC](https://www.ai4europe.eu/business-and-industry/case-studies/ai4czc), a winner project of [I-Nergy second open call](https://i-nergy.eu/2nd-open-calls-winning-projects). \
For any comments, subjections or requests you can reach us opening an issue or sendning an email to contact@inceptive.tech 

## About ENTSOE Transparency Platform

[ENTSOE Transparency Platform](https://transparency.entsoe.eu/dashboard/show) is the "central collection and publication of electricity generation, transportation and consumption data and information for the pan-European market." \

This project is nor related at all with ENTSOE, ENTSOE Transparency Platform or the company developing/maintaining transparency platform. \

You can get more information about transparency platform [here](https://transparency.entsoe.eu/content/static_content/Static%20content/knowledge%20base/knowledge%20base.html)

