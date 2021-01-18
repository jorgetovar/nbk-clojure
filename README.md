# Authorizer

Application that authorizes a transaction for a specific account following a set of predefined rules.

The program handles two kinds of operations, deciding on which one according to the line that is being processed:
1. Account creation
2. Transaction authorization

## Installation

### Verify java installation

    $ java -version
    
### Leiningen

[How to install Leiningen](https://purelyfunctional.tv/guide/how-to-install-clojure/)

## Usage

### Lein 

Pass stream of data to Lein

    $ lein run "{\"account\": {\"active-card\": true, \"available-limit\": 100}}" "{\"transaction\": {\"merchant\": \"Burger King\", \"amount\": 20, \"time\": \"2019-02-13T10:00:00.000Z\"}}" "{\"transaction\": {\"merchant\": \"Habbib's\", \"amount\": 90, \"time\": \"2019-02-13T11:00:00.000Z\"}}"

### Java artifact

Pass stream of data to java artifact

    $ lein uberjar
    $ java -jar target/uberjar/authorizer-0.1.0-SNAPSHOT-standalone.jar "{\"account\": {\"active-card\": true, \"available-limit\": 100}}" "{\"transaction\": {\"merchant\": \"Burger King\", \"amount\": 20, \"time\": \"2019-02-13T10:00:00.000Z\"}}" "{\"transaction\": {\"merchant\": \"Habbib's\", \"amount\": 90, \"time\": \"2019-02-13T11:00:00.000Z\"}}"

## Options

Stream of data with accounts or transactions

```
{"account": {"active-card": true, "available-limit": 100}}
{"transaction": {"merchant": "Burger King", "amount": 20, "time": "2019-02-13T10:00:00.000Z"}}
{"transaction": {"merchant": "Habbib's", "amount": 90, "time": "2019-02-13T11:00:00.000Z"}}
```
## Testing 

Run the test for the Authorizer Account logic and Adapter stream reader

    $ lein test   
    
## Business rules

You should implement the following rules, keeping in mind new rules will appear in the future:
- No transaction should be accepted without a properly initialized account:
account-not-initialized
- No transaction should be accepted when the card is not active: ​card-not-active
- The transaction amount should not exceed available limit: ​insufficient-limit
- There should not be more than 3 transactions on a 2 minute interval:
high-frequency-small-interval
- There should not be more than 1 similar transactions (same amount and merchant) in a 2 minutes interval: ​doubled-transaction


## License

Copyright © 2021 

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
