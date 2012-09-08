function leagueUpdated(response) {
    $('#league-heading').html($('<h2>' + response + '</h2>'));
}

$(document).ready(function () {
    var $tabs = $(".tabs");
    $tabs.tabs({
//            cache:true,
        ajaxOptions:{
            cache:false,
            dataFilter:function (data) {
                var $content = $(data).find("div #content");
                return $content.length > 0 ? $content : $(data);
            }
        }
    });

    $tabs.on('tabsload', function (e, ui) {
        console.log("Tab loaded: " + ui.tab);
    });

    $('.team-schedule').each(function () {
        var $link = $(this);
        var $dialog = $('<div></div>')
            .dialog({
                autoOpen:false,
                resizable:true,
                title:$link.attr('title'),
                width:500,
                maxHeight:800
            });

        $link.click(function () {
            if (!$dialog.is("div #content")) {
                $dialog.load($link.attr('href') + ' #content')
            }
            $dialog.dialog('open');

            return false;
        });
    });

    // delegate style - will bind this event when #week-select-scores is created.
    $("#admin-panel").on("change", "#week-select-scores", function (e) {
        jsRoutes.controllers.WeekController.editWeekScores(e.target.options[e.target.selectedIndex].value)
            .ajax({success:function (response) {
                $("#week-select-scores-content").html(response);
            } });
    });
});
