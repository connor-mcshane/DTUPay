Feature: Request barcode
  This feature tests the system functionality in relation to a customer requesting a barcode from DTU Pay (the system). 
	
	Scenario: Customer requests a barcode but is not registered in DTU Pay
		Given A customer with cpr "021136-4491"
		When he requests a barcode from DTU Pay 
		Then DTU Pay returns an error code 400
		
	Scenario: Customer requests a barcode and is registered in DTU Pay
		Given A customer with cpr "579124-0660"
		And he is registered in DTU Pay
		When he requests a barcode from DTU Pay
		Then DTU Pay returns a barcode