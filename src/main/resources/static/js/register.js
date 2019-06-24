function isUserName(text){
    if(text.length<6)
        return 0;
    else if(text.length>20)
        return 1;
    if(!/^\w+$/.test(text))
        return 2;
    return 3;
}

function checkUserName(){
    switch (isUserName($("#username").val())) {
        case 0:
            alert("用户名过短……");
            return false;
        case 1:
            alert("用户名过长……");
            return false;
        case 2:
            alert("用户名含有非法字符……");
            return false;
    }
    return true;
}

function isPassword(text){
    if(text.length<10)
        return 0;
    else if(text.length>30)
        return 1;
    if(!/^(\w|[!@#$%^&*(),.:;<>?~`\-+=\/|[\]{} '"])+$/.test(text))
        return 2;
    return 3;
}

function checkPassword(){
    switch (isPassword($("#password").val())) {
        case 0:
            alert("密码过短……");
            return false;
        case 1:
            alert("密码过长……");
            return false;
        case 2:
            alert("密码含有非法字符……");
            return false;
    }
    return true;
}

function isNickname(text){
    if(text.length<2)
        return 0;
    else if(text.length>30)
        return 1;
    return 2;
}

function checkNickname(){
    switch (isNickname($("#nickname").val())) {
        case 0:
            alert("昵称过短……");
            return false;
        case 1:
            alert("昵称过长……");
            return false;
    }
    return true;
}

function isPhone(text){
    if(text.length>30)
        return 0;
    if(!/^\+?[0-9]*$/.test(text))
        return 1;
    return 2;
}

function checkPhone(){
    switch (isPhone($("#phone").val())) {
        case 0:
            alert("电话号码过长……");
            return false;
        case 1:
            alert("电话号码含有非法字符……");
            return false;
    }
    return true;
}

$(document).ready(function(){
    $("#register").click(function() {
        if (!checkUserName())
            return;
        if (!checkPassword())
            return;
        if ($("#password").val()!=$("#pswdck").val()){
            alert("两次输入密码不一致……");
            return;
        }
        if (!checkNickname())
            return;
        if ($("#mail").val().length > 100) {
            alert("邮箱过长……");
            return;
        }
        if (!checkPhone())
            return;
        if ($("#address").val().length > 100) {
            alert("地址过长……");
            return;
        }

        let user = {};
        user.username = $("#username").val();
        alert(user.username)
        user.password = $("#password").val();
        user.nickname = $("#nickname").val();
        user.mail = $("#mail").val();
        user.phone = $("#phone").val();
        user.address = $("#address").val();

        $.ajax({
            url: "/user/register",
            data: user,
            type: "GET",
            contentType: "application/json;charset=utf-8",
            success: function(response){
                let info = JSON.parse(response);
                if(info.State==="SUCCESS"){
                    alert("注册成功(≥v≤)");
                }
                else
                    switch (info.Reason) {
                        case "USERNAME_EXIST":
                            alert("用户名已存在……");
                            break;
                        default:
                            alert(info.Reason);
                    }
            }
        });

    });
});