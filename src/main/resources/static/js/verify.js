!(function(window, document) {
    var size = 4;
    function Verify(container) {
        this.options = {
            id: container,
            canvasId: "verifyCanvas",
            width: "100",
            height: "30",
            code: "",
            charset: "0,1,2,3,4,5,6,7,8,9,a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z,A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z".split(",")
        }

        this._init();
        this.refresh();
    }

    Verify.prototype = {
        _init: function() {
            var con = document.getElementById(this.options.id);
            var canvas = document.createElement("canvas");
            canvas.id = this.options.canvasId;
            canvas.width = this.options.width;
            canvas.height = this.options.height;
            canvas.innerHTML = "您的浏览器版本不支持canvas";
            if(con.firstChild)
                con.replaceChild(canvas,con.firstChild);
            else
                con.appendChild(canvas);
        },

        refresh: function() {
            this.options.code = "";
            var canvas = document.getElementById(this.options.canvasId);
            if(canvas.getContext) {
                var ctx = canvas.getContext('2d');
            }else{
                return;
            }

            ctx.textBaseline = "middle";

            ctx.fillStyle = randomColor(180, 240);
            ctx.fillRect(0, 0, this.options.width, this.options.height);

            for(var i = 0; i < size; i++) {
                var txtArr = this.options.charset;
                var txt = txtArr[randomNum(0, txtArr.length)];
                this.options.code += txt;
                ctx.font = randomNum(this.options.height/2, this.options.height) + 'px SimHei';
                ctx.fillStyle = randomColor(50, 160);
                ctx.shadowOffsetX = randomNum(-3, 3);
                ctx.shadowOffsetY = randomNum(-3, 3);
                ctx.shadowBlur = randomNum(-3, 3);
                ctx.shadowColor = "rgba(0, 0, 0, 0.3)";
                var x = this.options.width / (size+1) * (i+1);
                var y = this.options.height / 2;
                var deg = randomNum(-30, 30);
                ctx.translate(x, y);
                ctx.rotate(deg * Math.PI / 180);
                ctx.fillText(txt, 0, 0);
                ctx.rotate(-deg * Math.PI / 180);
                ctx.translate(-x, -y);
            }
            for(var i = 0; i < size; i++) {
                ctx.strokeStyle = randomColor(40, 180);
                ctx.beginPath();
                ctx.moveTo(randomNum(0, this.options.width), randomNum(0, this.options.height));
                ctx.lineTo(randomNum(0, this.options.width), randomNum(0, this.options.height));
                ctx.stroke();
            }
            for(var i = 0; i < this.options.width/4; i++) {
                ctx.fillStyle = randomColor(0,255);
                ctx.beginPath();
                ctx.arc(randomNum(0, this.options.width), randomNum(0, this.options.height), 1, 0, 2 * Math.PI);
                ctx.fill();
            }
        },

        validate: function(code){
            var code = code.toLowerCase();
            var v_code = this.options.code.toLowerCase();
            return code == v_code;
        }
    }

    function randomNum(min, max) {
        return Math.floor(Math.random() * (max - min) + min);
    }

    function randomColor(min, max) {
        var r = randomNum(min, max);
        var g = randomNum(min, max);
        var b = randomNum(min, max);
        return "rgb(" + r + "," + g + "," + b + ")";
    }

    window.Verify = Verify;
})(window, document);