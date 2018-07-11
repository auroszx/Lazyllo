//All of the required JS is here.

//////////////////////////////////////////////////////////////////////////////////
//Variables and stuff
//////////////////////////////////////////////////////////////////////////////////
var refid;

//////////////////////////////////////////////////////////////////////////////////
//Functions and events?
//////////////////////////////////////////////////////////////////////////////////


//Detect clicks on boards and cards.
document.onclick = function(e) {
	//Clicked the content.
	if (e.target.getAttribute("class") == "card" || e.target.getAttribute("class") == "cardcontent") {
	  refid = e.target.getAttribute("refid");
	  getColumns(refid);
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
	  
	}
	//Clicked the edit button.
	if (e.target.getAttribute("class") == "edit") {
	  refid = e.target.parentElement.getAttribute("refid");
	  var bname = e.target.parentElement.childNodes[2].childNodes[0].childNodes[0].innerHTML;
	  console.log(bname);
	  var edit_board_name = document.getElementById("edit_name");
	  edit_board_name.value = bname;
	}
}


//Previously used for debugging.
function logResponse(text) {
	console.log(JSON.parse(text));
}

//Handles all responses client-side and preforms appropiate actions.
function handleResponse(response) {
	var res = JSON.parse(response);
	/*if (res.status >= 200 && res.status < 300) {
		if (res.msg != undefined && res.msg != null) {
			console.log(res.msg);
		}	
	}
	else {
		console.log("Error: ", res.status);
		console.log("Error message: ", res.msg);
		console.log("Full response: ", res);	
	}*/
	//logResponse(res);
	console.log(res);

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
	xhr("POST", fd, "/TrelloProject/BoardsServlet", handleResponse);
}

//Deletes a boards using its id sabed in attribute.
function deleteBoard(board_id) {
	xhr("DELETE", "", "/TrelloProject/BoardsServlet/"+board_id, handleResponse);
}

//Edits the board using it's id and parameters to be updated.
function editBoard() {
	var board_name = document.getElementById("edit_name").value;
	var board_id = refid;
	xhr("PUT", "", "/TrelloProject/BoardsServlet/edit?board_id="+board_id+"&board_name="+board_name, handleResponse);
}

//Gets all data from a board. Has permission verification. Data is shown in the same place.
function getBoardData() {
	var f = document.createElement("FORM");
	f.enctype = "multipart/form-data"; //Very sketchy.
	var fd = new FormData(f);
	fd.append("board_id", refid);

	xhr("POST", fd, "/TrelloProject/CardsServlet", handleResponse);
}

//Gets columns for a board
function getColumns(board_id) {
	var boardlist = document.getElementById("boardlist");

	xhr("GET", "", "/TrelloProject/Main/Data/ColumnsServlet/"+board_id, function(res) {
		var data = JSON.parse(res);
		var columns = data.columns;
		handleResponse(res);

		while (boardlist.firstChild) {
		    boardlist.removeChild(boardlist.firstChild);
		}

		for (var i in columns) {

			var boarddiv = document.createElement("div");
			boarddiv.setAttribute("class", "card");
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

			/*var a3 = document.createElement("a");
			a3.innerHTML = "&#x271A";
			a3.setAttribute("class", "create-card");
			a3.setAttribute("href", "#create-card");
			boarddiv.appendChild(a3);*/


			//var p = document.createElement("p");
			//p.innerHTML = "Created: "+columns[i].board_created_at;

			boarddiv.setAttribute("refid", columns[i].column_id);
			boarddivcontent.setAttribute("refid", columns[i].column_id);

			//boarddivcontent.appendChild(p);
			boarddiv.appendChild(boarddivcontent);
			boardlist.appendChild(boarddiv);
			
		}

		var createbtn = document.getElementById("createbtn");
		createbtn.innerHTML = "Create column";
		createbtn.setAttribute("href", "#create-column");

		var module = document.getElementById("module");
		module.innerHTML = "<a href='/TrelloProject/Main/'>"+module.innerHTML+"</a>";
		module.innerHTML = module.innerHTML+" > Board info"; //Needs tweaking


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

//Gets cards for a column
function getColumns(column_id) {
	var boardlist = document.getElementById("boardlist");

	xhr("GET", "", "/TrelloProject/Main/Data/CardsServlet/"+column_id, function(res) {
		var data = JSON.parse(res);
		var columns = data.columns;
		handleResponse(res);

		while (boardlist.firstChild) {
		    boardlist.removeChild(boardlist.firstChild);
		}

		for (var i in columns) {

			var boarddiv = document.createElement("div");
			boarddiv.setAttribute("class", "card");
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


			//var p = document.createElement("p");
			//p.innerHTML = "Created: "+columns[i].board_created_at;

			boarddiv.setAttribute("refid", columns[i].column_id);
			boarddivcontent.setAttribute("refid", columns[i].column_id);

			//boarddivcontent.appendChild(p);
			boarddiv.appendChild(boarddivcontent);
			boardlist.appendChild(boarddiv);
			
		}

		var createbtn = document.getElementById("createbtn");
		createbtn.innerHTML = "Create column";
		createbtn.setAttribute("href", "#create-column");

		var module = document.getElementById("module");
		module.innerHTML = "<a href='/TrelloProject/Main/'>"+module.innerHTML+"</a>";
		module.innerHTML = module.innerHTML+" > Board info"; //Needs tweaking


	});
}