# DTUPay
This was a university project that was completed over the course of 3 weeks by:
FRED Software Development, which includes:
- Andreas Stensig Jensen 	S134408
- Claudia-Ioana Satnoianu 	S172392
- Connor Mcshane		S171347
- Jonas Lind Dencker Otte	S047620
- Lina Mhiri			S171361
- Thomas Robert Grosman	S131058

# Executive Summary

The DTUPay software project included the creation of a transaction payment handling system that is intended to complete transactions between Merchants and Customers via interfacing with a simulated bank system (Fast Money Bank).
The DTUPay system is a microservice based architecture system and its implementation will be explained in this document. Also the projects repository (read only) is accesible via : git://repos.gbar.dtu.dk/s171347/Delhi-Repo.git 

#System Architecture
The DTUPay system is made out of 4 principle components which include:
- REST service programs that can receive requests required to complete a DTUPay transaction through the DTUPay API.
- A set of Message Driven Beans (MDB) that handle the internal business logic of DTUPay.
- An internal Database to keep track of DTUPay users (i.e Merchants and Customers).
- A set of MDBs which provide access to the Bank (Fast Money Bank) to the other MDBs within DTUPay.
Also a set of example programs were developed to demonstrate how a Merchant and or Customer can interface and utilise the DTUPay system. The simulators access the DTUPay services externally. This is achieved using a REST architectural style. The Customer simulator uses Http requests to create DTUPay users, to delete  DTUPay users, to obtain barcodes and to get the amount of a user’s last transaction. The Merchant simulator uses HTTP requests to execute transactions. JSON is used for data transfer.

I more comprehensive description of the project is available on a published google document via here https://docs.google.com/document/d/e/2PACX-1vQIcQvXTw96ACItXcgJ6v_opHNK2tL3l4n5-hUE603tvHSPUK9-dwJSXFdMnjfLOjiV_2NlVeagZieX/pub
