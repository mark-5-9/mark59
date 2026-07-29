/*
  Copyright 2023 Mark59.com
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.

*/

	function createCipher() {
		var xhttp = new XMLHttpRequest();
		xhttp.onreadystatechange = function() {
			if (this.readyState == 4 && this.status == 200) {
				populatePasswordCipher(this.responseText)
			}
		};
		xhttp.open("GET", encodeURI("api/cipher?pwd="	+ document.getElementById('otherdata').value), true);
		xhttp.send();
	}
	
	function populatePasswordCipher(pwdCipher) {
		var passwordCipherId = document.getElementById('otherdata');
		passwordCipherId.value = pwdCipher;
		//document.getElementById('otherdata').value = '';
		document.getElementById("createCipherBtn").disabled = true;
	}
	
	function enableOrdisableCreateCipherBtn() {
		if (document.getElementById('otherdata') !== null){
			if (isEmpty(document.getElementById('otherdata').value)) {
				document.getElementById("createCipherBtn").disabled = true;
			} else {
				document.getElementById("createCipherBtn").disabled = false;
			}
		}
	}



	function isEmpty(str) {
		return str === null || str === ""
	}

	
	function trimkey(key) {
		key.value = key.value.trim();
	}

	function hideSubmitBtn() {
		document.getElementById("submit").style.display = 'none';
		document.getElementById("loading").style.display = 'block';
	}
		