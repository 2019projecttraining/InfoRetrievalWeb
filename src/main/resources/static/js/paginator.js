(function ($) {
    $.fn.paginator = function (options) {
        //thisָ��ǰ��ѡ����
        var config = {
            url: "",
            pageParent: "",
            limit: 10,
            totalBars: 100,
            offset: 1,
            callback: null
        }
        //�ϲ�����
        var opts = $.extend(config, options);
 
        opts.totalBars = Math.ceil(opts.totalBars / opts.limit);
        //���㰴ť���ܸ���
 
        //��ȡoffset����
        var queryString = function (url) {
            var offset = (url.split("?")[1]).split("=")[1];
            return parseInt(offset);
        }
 
        //ajax���ķ��������ڷ�ҳ�����ݲ���
        var ajaxCore = function (offset, fn) {
            $.ajax({
                "url": opts.url,
                "data": {
                    "offset": offset,
                    "limit": opts.limit
                },
                "dataType": "JSON",
                "method": "POST",
                "success": fn
            });
        }
 
        //����װ���ҳ��ť
        var pageCore = function (offset) {
            if (opts.offset == offset) {
                return;
            } //����ǵ�ǰҳ�棬��ô��ʲô�¶����ø��ˣ�
            else {
                ajaxCore(offset, opts.callback);
                $(opts.pageParent).empty();
                //����������еĽڵ㣬������DOM�����µķ�ҳ��ť
                var output = "";
                var nextBar = offset == opts.totalBars ? "<li class=\"am-disabled\"><a yxhref=\"javascript:;\">?</a></li>" : "<li><a yxhref=\"" + opts.url + (offset + 1) + "\">?</a></li>";
                var preBar = offset == 1 ? "<li class=\"am-disabled\"><a yxhref=\"javascript:;\">?</a></li>" : "<li><a yxhref=\"" + opts.url + (offset - 1) + "\">?</a></li>";
                //��װ����һ���ڵ����һҳ�ڵ�
                if (opts.totalBars > 7) {
                    if (offset < 5) {
                        output += preBar;
                        for (var i = 1; i <= 5; i++) {
                            if (i == offset) {
                                output += "<li class=\"am-active\"><a yxhref=\"" + opts.url + offset + "\">" + offset + "</a></li>";
                            } else {
                                output += "<li><a yxhref=\"" + opts.url + i + "\">" + i + "</a></li>";
                            }
                        }
                        output += "<li><span>...</span></li>";
                        output += "<li><a yxhref=\"" + opts.url + (opts.totalBars) + "\">" + (opts.totalBars) + "</a></li>" + nextBar;
                    } else if (offset >= 5 && offset <= opts.totalBars - 4) {
                        //��ҳ�����7����ʱ����ô�ڵ�����͵��������ʱ��ִ��
                        output += preBar;
                        output += "<li><a yxhref=\"" + opts.url + 1 + "\">" + 1 + "</a></li>";
                        //��һ��
                        output += "<li><span>...</span></li>"; //ʡ�Ժ�
 
                        output += "<li><a yxhref=\"" + opts.url + (offset - 1) + "\">" + (offset - 1) + "</a></li>";
 
                        output += "<li class=\"am-active\"><a  yxhref=\"" + opts.url + offset + "\">" + offset + "</a></li>";
 
                        output += "<li><a yxhref=\"" + opts.url + (offset + 1) + "\">" + (offset + 1) + "</a></li>";
 
                        output += "<li><span>...</span></li>"; //ʡ�Ժ�;
 
                        output += "<li><a yxhref=\"" + opts.url + (opts.totalBars) + "\">" + (opts.totalBars) + "</a></li>"; //βҳ
 
                        output += nextBar;
 
                    } else if (offset > opts.totalBars - 4 && offset <= opts.totalBars) {
                        //��ҳ��λ�ڵ������ĸ�ʱ��
                        output += preBar;
                        output += "<li><a yxhref=\"" + opts.url + 1 + "\">" + 1 + "</a></li>" + "<li><span>...</span></li>";
 
                        for (var j = 4; j >= 0; j--) {
                            if (opts.totalBars - j == offset) {
                                output += "<li class=\"am-active\"><a yxhref=\"" + opts.url + (opts.totalBars - j) + "\">" + (opts.totalBars - j) + "</a></li>";
                            } else {
                                output += "<li><a yxhref=\"" + opts.url + (opts.totalBars - j) + "\">" + (opts.totalBars - j) + "</a></li>";
                            }
                        }
                        output += nextBar;
                    } else {
                        console.log("��ҳ���ݳ���");
                        return;
                    }
                } else {
                    output += preBar;
                    for (var i = 1; i <= opts.totalBars; i++) {
                        if (i == offset) {
                            output += "<li class=\"am-active\"><a yxhref=\"" + opts.url + offset + "\">" + offset+ "</a></li>";
                        } else {
                            output += "<li><a yxhref=\"" + opts.url + i + "\">" + i+ "</a></li>";
                        }
                    }
                    output += nextBar;
                }
                $(opts.pageParent).append(output);
                opts.offset = offset; //��ƫ������ֵ��config�����offset
            }
        }
 
        //����������ֹ����¼������¼����ҳ
        var clear = function () {
            $(opts.pageParent).empty().undelegate();
        }
 
 
        //��ʼ��װ���ҳ��ť
        var init = function (fn) {
            if (typeof (fn) != "function") {
                console.log("��������ȷ��ִ�лص�����");
            } else {
                opts.callback = fn;
            }
            clear();
            ajaxCore(1, opts.callback);//ִ�г�ʼ��ajax����
            var preBar = "<li class=\"am-disabled\"><a yxhref=\"javascript:;\">?</a></li>";
            //��һҳ,�����õ�Ч����
            //���ֻ��һҳ����ô������һҳ
            var nextBar = opts.totalBars > 1 ? "<li><a yxhref=\"" + opts.url + 2 + "\">?</a></li>" : "<li class=\"am-disabled\"><a yxhref=\"javascript:;\">?</a></li>";
            //���һҳ
            var output = "<li class=\"am-active\"><a yxhref=\"" + opts.url + 1 + "\">1</a></li>";
 
            if (opts.totalBars <= 7) {
                for (var i = 1; i < opts.totalBars; i++) {
                    output += "<li><a yxhref=\"" + opts.url + (i + 1) + "\">" + (i + 1) + "</a></li>";
                }
            } else {
                for (var j = 1; j < 5; j++) {
                    output += "<li><a yxhref=\"" + opts.url + (j + 1) + "\">" + (j + 1) + "</a></li>";
                }
                output += "<li><span>...</span></li>";
                output += "<li><a yxhref=\"" + opts.url + (opts.totalBars) + "\">" + (opts.totalBars) + "</a></li>";
            }
            $(opts.pageParent).delegate("a","click", function () {
                var offset = queryString($(this).attr("yxhref"));
                console.log("ok");
                pageCore(offset);
            });
            $(opts.pageParent).append(preBar + output + nextBar);
        };
        init(opts.callback);//��ʼ����ҳ����
    }
}(window.jQuery))