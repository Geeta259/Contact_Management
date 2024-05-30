const toggleSidebar=()=>{
	if($(".sidebar").is(":visible"))
	{
	    $(".sidebar").css("display","none");
	    $(".content").css("margin-left","0%");	
	}
	else
	{
		 $(".sidebar").css("display","block");
	    $(".content").css("margin-left","20%");
	}
};

function deleteContact(cid)
{
	swal({
	  title: "Are you sure?",
	  text: "You want to delete this contact?",
	  icon: "warning",
	  buttons: true,
	  dangerMode: true,
	})
	.then((willDelete) => {
	  if (willDelete) {
	     window.location="/user/delete/"+cid;
	    
	  } else {
	    swal("Your contact is safe!");
	  }
	});
}

const search=()=>{
	let query = $("#search-input").val();
	if(query==='')
	{
		$(".search-result").hide();	
	}else{
		console.log(query);
		
		//sending request to server
		let url=`http://localhost:8081/search/${query}`;
		fetch(url)
		.then((response) => {
			return response.json();
		})
		.then((data)=>{
			//data....
			//console.log(data);
			let text = `<div class='list-group'>`
			data.forEach((contact)=>{
				text+=`<a href='/user/${contact.cid}/contact' class='list-group-item list-group-item-action'> ${contact.name} </a>`;	
			});
			
			text+=`</div>`;
			
			$(".search-result").html(text);
			$(".search-result").show();
		});
		
	}
}
