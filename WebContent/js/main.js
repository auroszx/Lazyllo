//All of the required JS is here.

//////////////////////////////////////////////////////////////////////////////////
//Variables and stuff
//////////////////////////////////////////////////////////////////////////////////
var refid;

//////////////////////////////////////////////////////////////////////////////////
//Functions and events?
//////////////////////////////////////////////////////////////////////////////////


//Detect clicks on boards, columns and cards.
document.onclick = function(e) {
	// console.log(e.target.parentElement);
	// console.log(e.target.parentElement.parentElement.getAttribute("class"));
	// console.log(e.target.getAttribute("class"));
	
	//Click detection for GET and DELETE.
	if (e.target.parentElement != undefined) {
		//Clicked the content.
		if ((e.target.getAttribute("class") == "card" || e.target.getAttribute("class") == "cardcontent") && e.target.getAttribute("type") == "board") {
		  refid = e.target.getAttribute("refid");
		  console.log("Getting columns...");
		  var bname3 = "";
		  if (e.target.getAttribute("class") == "card") {
		  	bname3 = e.target.childNodes[2].firstChild.innerText;
		  }
		  else if (e.target.getAttribute("class") == "cardcontent") {
		  	bname3 = e.target.firstChild.innerText;
		  }
		  getColumns(refid, bname3);
		}
		//Clicked the create card button.
		if (e.target.parentElement.getAttribute("class") == "card column" && e.target.getAttribute("class") == "create-card") {
		  refid = e.target.parentElement.getAttribute("refid");
		}
		//Clicked the delete button.
		if (e.target.getAttribute("class") == "erase") {
		  refid = e.target.parentElement.getAttribute("refid");
		  if (e.target.parentElement.getAttribute("type") == "board") {
		  	deleteBoard(refid);
		  }
		  else if (e.target.parentElement.getAttribute("type") == "column") {
		  	deleteColumn(refid);
		  }
		  else if (e.target.parentElement.getAttribute("type") == "card") {
		  	deleteCard(refid);
		  }
		  else if (e.target.parentElement.parentElement.parentElement.getAttribute("type") == "perm") {
		  	refid = e.target.parentElement.parentElement.parentElement.getAttribute("refid");
		  	deleteBoardPerm(refid);
		  }
		  
		}
	}

	//Clicked the edit button.
	if (e.target.getAttribute("class") == "edit") {
		//For boards.
		if (e.target.parentElement.getAttribute("type") == "board") {
		  refid = e.target.parentElement.getAttribute("refid");
		  var bname = e.target.parentElement.childNodes[2].childNodes[0].childNodes[0].innerHTML;
		  console.log(bname);
		  var edit_board_name = document.getElementById("edit_name");
		  edit_board_name.value = bname;
		}
		//For columns.
		else if (e.target.parentElement.getAttribute("type") == "column") {
			refid = e.target.parentElement.getAttribute("refid");
			var bname = e.target.parentElement.childNodes[3].childNodes[0].innerText;
			console.log(bname);
			var edit_board_name = document.getElementById("edit_cname");
			edit_board_name.value = bname;
		}
		//For cards.
		else if (e.target.parentElement.getAttribute("type") == "card") {
			refid = e.target.parentElement.getAttribute("refid");
			var bname = e.target.parentElement.childNodes[3].childNodes[0].innerText;
			console.log(bname);
			var edit_board_name = document.getElementById("edit_caname");
			edit_board_name.value = bname;

			var bdes = e.target.parentElement.childNodes[3].childNodes[2].innerText;
			console.log(bdes);
			var edit_board_des = document.getElementById("edit_cades");
			edit_board_des.value = bdes;
		}
	}
}


//Previously used for debugging.
function logResponse(text) {
	console.log(JSON.parse(text));
}

//Handles all responses client-side and preforms appropiate actions.
function handleResponse(response) {
	var res = JSON.parse(response);
	
	if ((res.status >= 200 && res.status < 300) || res.status == undefined) {
		console.log(res);
	}
	else {
		console.error(res);	
	}

	if (res.redirect != undefined && res.redirect != null) {
		redirect(res.redirect);	
	}

	if (res.boards != undefined && res.boards != null) {
		return res.boards;
	}

}

//Redirects the hell out of your browser.
function redirect(location) {
	window.location.href = location;
}

//Glue code to append elements after another.
function insertAfter(newNode, referenceNode) {
    referenceNode.parentNode.insertBefore(newNode, referenceNode.nextSibling);
}

//Signs up new users.
function signup() {
	var signupform = document.getElementById("signup");
	var fd = new FormData(signupform);
	xhr("POST", fd, "/TrelloProject/RegisterServlet", handleResponse);

}

//Very obvious.
function login() {
	var loginform = document.getElementById("login");
	var fd = new FormData(loginform);
	xhr("POST", fd, "/TrelloProject/LoginServlet", handleResponse);
}

//Also obvious.
function logout() {
	xhr("GET", "", "/TrelloProject/LogoutServlet", handleResponse);
}

//Used to load this at Main <head>, not used anymore.
function maintest() {	
	xhr("GET", "", "/TrelloProject/TrelloMain", handleResponse);
	
}

//Gets all boards from the server and shows them.
function getBoards() {
	var boardlist = document.getElementById("boardlist");

	while (boardlist.firstChild) {
	    boardlist.removeChild(boardlist.firstChild);
	}

	xhr("GET", "", "/TrelloProject/BoardsServlet", function(res) {
		var data = JSON.parse(res);
		var boards = data.boards;
		handleResponse(res);

		for (var i in boards) {

			var boarddiv = document.createElement("div");
			boarddiv.setAttribute("class", "card");
			boarddiv.setAttribute("type", "board");

			var boarddivcontent = document.createElement("div");
			boarddivcontent.setAttribute("class", "cardcontent");
			boarddivcontent.setAttribute("type", "board");
			var h4 = document.createElement("h4");
			h4.innerHTML = "<b>"+boards[i].board_name+"</b>";
			boarddivcontent.appendChild(h4);

			var a2 = document.createElement("a");
			a2.innerHTML = "&#x2716";
			a2.setAttribute("class", "erase");
			boarddiv.appendChild(a2);

			var a1 = document.createElement("a");
			a1.innerHTML = "&#x270D";
			a1.setAttribute("class", "edit");
			a1.setAttribute("href", "#edit-board");
			boarddiv.appendChild(a1);


			var p = document.createElement("p");
			p.innerHTML = "Created: "+boards[i].board_created_at;

			boarddiv.setAttribute("refid", boards[i].board_id);
			boarddivcontent.setAttribute("refid", boards[i].board_id);

			boarddivcontent.appendChild(p);
			boarddiv.appendChild(boarddivcontent);
			boardlist.appendChild(boarddiv);
			
		}

	});
	
}

//Creates new boards.
function createBoard() {
	var createboard = document.getElementById("createboard");
	var fd = new FormData(createboard);
	xhr("POST", fd, "/TrelloProject/BoardsServlet", function() {
		handleResponse;
		getBoards();

	});
}

//Deletes a boards using its id sabed in attribute.
function deleteBoard(board_id) {
	xhr("DELETE", "", "/TrelloProject/BoardsServlet/"+board_id, function() {
		handleResponse;
		getBoards();

	});
}

//Edits the board using it's id and parameters to be updated.
function editBoard() {
	var board_name = document.getElementById("edit_name").value;
	var board_id = refid;
	xhr("PUT", "", "/TrelloProject/BoardsServlet/edit?board_id="+board_id+"&board_name="+board_name, function() {
		handleResponse;
		getBoards();

	});
}

//Gets columns for a board
function getColumns(board_id, board_name) {
	var boardlist = document.getElementById("boardlist");
	localStorage.setItem("board_id", board_id);
	localStorage.setItem("board_name", board_name);

	xhr("GET", "", "/TrelloProject/Main/Data/ColumnsServlet/"+board_id, function(res) {
		var data = JSON.parse(res);
		var columns = data.columns;
		handleResponse(res);

		while (boardlist.firstChild) {
		    boardlist.removeChild(boardlist.firstChild);
		}

		for (var i in columns) {
			console.log("Creating column #", i);


			var boarddiv = document.createElement("div");
			boarddiv.setAttribute("class", "card column");
			boarddiv.setAttribute("type", "column");

			var boarddivcontent = document.createElement("div");
			boarddivcontent.setAttribute("class", "cardcontent");
			boarddivcontent.setAttribute("type", "column");
			var h4 = document.createElement("h4");
			h4.innerHTML = "<b>"+columns[i].column_name+"</b>";
			boarddivcontent.appendChild(h4);

			var a2 = document.createElement("a");
			a2.innerHTML = "&#x2716";
			a2.setAttribute("class", "erase");
			boarddiv.appendChild(a2);

			var a1 = document.createElement("a");
			a1.innerHTML = "&#x270D";
			a1.setAttribute("class", "edit");
			a1.setAttribute("href", "#edit-column");
			boarddiv.appendChild(a1);

			var a3 = document.createElement("a");
			a3.innerHTML = "&#x271A";
			a3.setAttribute("class", "create-card");
			a3.setAttribute("href", "#create-card");
			boarddiv.appendChild(a3);

			boarddiv.setAttribute("refid", columns[i].column_id);
			boarddivcontent.setAttribute("refid", columns[i].column_id);

			var spacer = document.createElement("br");

			
			boarddiv.appendChild(boarddivcontent);
			boardlist.appendChild(boarddiv);	

			//console.log(boarddiv);		
			
		}


		xhr("GET", "", "/TrelloProject/Main/Data/CardsServlet/", function(res) {

			var data = JSON.parse(res);
			var cards = data.cards;
			handleResponse(res);

			if (cards.length != undefined || cards.length > 0) {

				for (var i in boardlist.childNodes) {
					if (i < boardlist.childNodes.length) {
						var col_id = boardlist.childNodes[i].attributes[2].nodeValue;


						//Create cards inside column
						for (var j in cards) {
							if (cards[j].column_id == col_id) {

								console.log("Column #", col_id, "with card #", cards[j].card_id);

								var columns = document.getElementsByClassName("card column");


								var boarddivcontent = boardlist.childNodes[i];
								//console.log(boarddivcontent);

								var boarddiv2 = document.createElement("div");
								boarddiv2.setAttribute("class", "card realcard");
								boarddiv2.setAttribute("type", "card");

								var boarddivcontent2 = document.createElement("div");
								boarddivcontent2.setAttribute("class", "cardcontent");
								boarddivcontent2.setAttribute("type", "card");
								var h4_2 = document.createElement("h4");
								h4_2.innerHTML = "<b>"+cards[j].card_name+"</b>";
								boarddivcontent2.appendChild(h4_2);

								var simplespacer = document.createElement("br");
								boarddivcontent2.appendChild(simplespacer);

								var p_2 = document.createElement("p");
								p_2.innerHTML = ""+cards[j].card_description+"";
								boarddivcontent2.appendChild(p_2);

								var a2_2 = document.createElement("a");
								a2_2.innerHTML = "&#x2716";
								a2_2.setAttribute("class", "erase");
								boarddiv2.appendChild(a2_2);

								var a1_2 = document.createElement("a");
								a1_2.innerHTML = "&#x270D";
								a1_2.setAttribute("class", "edit");
								a1_2.setAttribute("href", "#edit-card");
								boarddiv2.appendChild(a1_2);

								var a3_2 = document.createElement("a");
								a3_2.innerHTML = "&#x271A";
								a3_2.setAttribute("class", "create-card");
								a3_2.setAttribute("href", "#create-card");
								boarddiv2.appendChild(a3_2);

								boarddiv2.setAttribute("refid", cards[j].card_id);
								boarddivcontent2.setAttribute("refid", cards[j].card_id);

								boarddiv2.appendChild(boarddivcontent2);
								boarddivcontent.appendChild(boarddiv2);
								

							}
						}

							
						
					}
				}
			}//End of if.

		});

		var createbtn = document.getElementById("createbtn");
		createbtn.innerHTML = "Create column";
		createbtn.setAttribute("href", "#create-column");

		var module = document.getElementById("module");
		module.innerHTML = "<a href='/TrelloProject/Main/'>"+module.innerHTML+"</a>";
		module.innerHTML = module.innerHTML+" > "+board_name; //Needs tweaking

		//<a id="permbtn" class="btn" href="#board-perm">Board permissions</a>

		var permbtn = document.createElement("a");
		permbtn.setAttribute("id", "permbtn");
		permbtn.setAttribute("class", "btn");
		permbtn.setAttribute("href", "#board-perm");
		permbtn.setAttribute("style", "margin-left: 4px;");
		permbtn.setAttribute("onclick", "getBoardPermList()");
		permbtn.innerHTML = "Board permissions";

		insertAfter(permbtn, createbtn);

	});
}

//Creates a new column inside the board.
function createColumn() {
	var createcolumn = document.getElementById("createcolumn");
	var fd = new FormData(createcolumn);
	fd.append("board_id", refid);
	xhr("POST", fd, "/TrelloProject/Main/Data/ColumnsServlet", handleResponse);
}

//Deletes a boards using its id sabed in attribute.
function deleteColumn(column_id) {
	xhr("DELETE", "", "/TrelloProject/Main/Data/ColumnsServlet/"+column_id, handleResponse);
}

//Edits a column using its id.
function editColumn() {
	var column_name = document.getElementById("edit_cname").value;
	var column_id = refid;
	xhr("PUT", "", "/TrelloProject/Main/Data/ColumnsServlet/edit?column_id="+column_id+"&column_name="+column_name, handleResponse);
}

//Creates a new column inside the board.
function createCard() {
	var createcard = document.getElementById("createcard");
	var fd = new FormData(createcard);
	fd.append("column_id", refid);
	xhr("POST", fd, "/TrelloProject/Main/Data/CardsServlet", handleResponse);
}


//Deletes a card. Of course.
function deleteCard(card_id) {
	console.log("Card ID: ", card_id);
	xhr("DELETE", "", "/TrelloProject/Main/Data/CardsServlet/"+card_id, handleResponse);
}

//Edits a column using its id.
function editCard() {
	var card_description = document.getElementById("edit_cades").value;
	var card_name = document.getElementById("edit_caname").value;
	var card_id = refid;
	xhr("PUT", "", "/TrelloProject/Main/Data/CardsServlet/edit?card_id="+card_id+"&card_name="+card_name+"&card_description="+card_description, handleResponse);
}

//Sets board permissions.
function setBoardPerm() {
	var boardperm = document.getElementById("boardperm");
	var fd = new FormData(boardperm);
	fd.append("board_id", localStorage.getItem("board_id"));
	if (document.getElementById("perm_username").value != "") {
		xhr("POST", fd, "/TrelloProject/BoardsServlet/setperm", function() {
			handleResponse;
			getBoardPermList();
		});
	}
}

//Gets the board permission list
function getBoardPermList() {
	var permlist = document.getElementById("permlist");
	var board_id = localStorage.getItem("board_id");

	xhr("GET", "", "/TrelloProject/BoardsServlet/getpermlist?board_id="+board_id, function(res) {
		var data = JSON.parse(res);
		var perms = data.perms;
		handleResponse(res);

		while (permlist.firstChild) {
		    permlist.removeChild(permlist.firstChild);
		}

		for (var k in perms) {
			var permdiv = document.createElement("div");
			permdiv.setAttribute("refid", perms[k].user_id);
			permdiv.setAttribute("type", "perm");

			var permtype = "";
			if (perms[k].type_board_user_id == 1) {
				permtype = "Board Master";
			}
			else {
				permtype = "Collaborator";
			}

			var permp = document.createElement("span");
			permp.innerHTML = "<pre style='margin-left: -15%;font-family: Arial'>		"+perms[k].user_username+"		"+permtype+"<a class='erase' style='margin-top: -18px'>&#x2716</a></pre>";
			permdiv.appendChild(permp);

			/*var a2_2 = document.createElement("a");
			a2_2.innerHTML = "&#x2716";
			a2_2.setAttribute("class", "erase");
			permdiv.appendChild(a2_2);*/

			permlist.appendChild(permdiv);
		}
	});
}

//Deletes board permission by user
function deleteBoardPerm(user_id) {
	console.log("User ID: ", user_id);
	var board_id = localStorage.getItem("board_id");
	console.log("Board ID: ", board_id);
	xhr("DELETE", "", "/TrelloProject/BoardsServlet/deleteperm?user_id="+user_id+"&board_id="+board_id, function() {
		handleResponse;
		getBoardPermList();
	});
}

function getBoardsByName() {
	var searchinput = document.getElementById("searchinput");
	var board_name = "%"+searchinput.value+"%";

	var boardlist = document.getElementById("boardlist");

	while (boardlist.firstChild) {
	    boardlist.removeChild(boardlist.firstChild);
	}

	xhr("GET", "", "/TrelloProject/BoardsServlet/boardsearch?board_name="+board_name, function(res) {
		handleResponse(res);

		var data = JSON.parse(res);
		var boards = data.boards;
		handleResponse(res);

		for (var i in boards) {

			var boarddiv = document.createElement("div");
			boarddiv.setAttribute("class", "card");
			boarddiv.setAttribute("type", "board");

			var boarddivcontent = document.createElement("div");
			boarddivcontent.setAttribute("class", "cardcontent");
			boarddivcontent.setAttribute("type", "board");
			var h4 = document.createElement("h4");
			h4.innerHTML = "<b>"+boards[i].board_name+"</b>";
			boarddivcontent.appendChild(h4);

			var a2 = document.createElement("a");
			a2.innerHTML = "&#x2716";
			a2.setAttribute("class", "erase");
			boarddiv.appendChild(a2);

			var a1 = document.createElement("a");
			a1.innerHTML = "&#x270D";
			a1.setAttribute("class", "edit");
			a1.setAttribute("href", "#edit-board");
			boarddiv.appendChild(a1);


			var p = document.createElement("p");
			p.innerHTML = "Created: "+boards[i].board_created_at;

			boarddiv.setAttribute("refid", boards[i].board_id);
			boarddivcontent.setAttribute("refid", boards[i].board_id);

			boarddivcontent.appendChild(p);
			boarddiv.appendChild(boarddivcontent);
			boardlist.appendChild(boarddiv);
			
		}
	});
}

