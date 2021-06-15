# data-services-directory

## License

All rights to the results that are made available via this repository are owned by their respective creators, as identified in the relevant file names. Unless explicitly indicated otherwise, the results are made available to you under the EUPL, Version 1.2, an EU approved open source licence. For a full version of the licence and guidance, please visit https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12

Note that the results are protected by copyright, and all rights which are not expressly licenced to you by the owners or granted by applicable law are explicitly reserved.

This repository is the only formal source of the results of the TOOP project, an action that was funded by the EU Horizon 2020 research and innovation programme under grant agreement No 737460 (see https://toop.eu/). If you have obtained the results elsewhere or under a different licence, it is likely that this is in violation of copyright law. In case of doubt, please contact us.  
## Running the service

1. Clone the repository via `git-clone`
2. Run `mvn verify`
3. Deploy `dsd-service/target/dsd-service-[VERSION].war` to an application container.

## Configuration

DSD service pulls its data from `TOOP-Directory`. By default, the directory address
is set to `http://directory.acc.exchange.toop.eu`. If you need to change this address and
make it point to another directory address, please use `TOOP_DIR_URL` as an environment 
variable or System property.
