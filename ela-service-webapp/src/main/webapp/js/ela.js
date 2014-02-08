/*
 * Copyright (c) 2014 Rafal Kijewski <kijes@kijes.com>
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

var USER_PROPERY = "user";
var USER_TABLE = "usertable";
var USER_DATA = "userTableDataModel";

function addUserTableRow(userObj) {
    var userId = "user"+userObj.user_id;
    var enterId = "enter"+userObj.user_id;
    var leaveId = "leave"+userObj.user_id;
    var deleteId = "delete"+userObj.user_id;
    var statusId = "status"+userObj.user_id;

    var tr = jQuery("<tr id=\""+userId+"\" class=\"userRow\"/>");
    tr.append("<td>" + userObj.user_id + "</td>");
    tr.append("<td>" + userObj.card_id + "</td>");
    tr.append("<td id=\""+statusId+"\">" + decodeUserStatus(userObj) + "</td>");
    tr.append("<td>" + createActionCell(enterId, leaveId, deleteId) + "</td>");

    tr.hide();

    jQuery("#"+USER_TABLE).append(tr);

    jQuery("#"+enterId)
        .button()
        .data(USER_PROPERY, userObj)
        .click(function() {
            handleEnterButton($(this).data(USER_PROPERY))
        });
    jQuery("#"+leaveId)
        .button()
        .data(USER_PROPERY, userObj)
        .click(function() {
            handleLeaveButton($(this).data(USER_PROPERY))
        });
    jQuery("#"+deleteId)
        .button()
        .data(USER_PROPERY, userObj)
        .click(function() {
            //$(this).button("option", "disabled", true);
            handleDeleteButton($(this).data(USER_PROPERY))
        });
}

function getUserTableDataModel() {
    var emptyArr = [];
    var existingUser = jQuery("#"+USER_TABLE).data(USER_DATA);
    if (existingUser != undefined) {
        return existingUser;
    }
    return emptyArr;
}

function changeFromLeftAreaToEnteredArea(oldEnteredArea, newEnteredArea) {
    if (oldEnteredArea == "false" && newEnteredArea == "true") {
        return true;
    }
    return false;
}

function createHashTableForUserIds(userObjArr) {
    var userObjIds = {}
    userObjArr.forEach(function(userObj){
        userObjIds[userObj.user_id] = userObj;
    });
    return userObjIds;
}

function purgeRemovedUserObjs(userTableDataModel) {
    var newUserTableDataModel = userTableDataModel.filter(function(userObjData){
        return (userObjData.remove == false);
    });
    return newUserTableDataModel;
}

function updateUserTableDataModel(newUserObjArr) {
    var newUserObjIds = createHashTableForUserIds(newUserObjArr);
    var userTableDataModel = purgeRemovedUserObjs(getUserTableDataModel());

    for (var i = 0; i < userTableDataModel.length; i++) {
        userTableDataModel[i].remove = false;
        userTableDataModel[i].create = false;
        userTableDataModel[i].leaveEnterChange = false;
        userTableDataModel[i].enterLeaveChange = false;

        var userObj = userTableDataModel[i].user;
        if (!(userObj.user_id in newUserObjIds)) {
            userTableDataModel[i].remove = true;
        } else {
            var newUserObj = newUserObjIds[userObj.user_id];
            delete newUserObjIds[userObj.user_id];
            if (userObj.entered_area != newUserObj.entered_area) {
                if (changeFromLeftAreaToEnteredArea(userObj.entered_area, newUserObj.entered_area)) {
                    userTableDataModel[i].leaveEnterChange = true;
                } else {
                    userTableDataModel[i].enterLeaveChange = true;
                }
            }
            userTableDataModel[i].user = newUserObj;
        }
    }

    for (var userObjId in newUserObjIds) {
        userTableDataModel.push( {
            user : newUserObjIds[userObjId],
            remove : false,
            create : true,
            enterLeaveChange : false,
            leaveEnterChange : false
        });
    }
    return userTableDataModel;
}

function createUserRowSelectorToRemove(userTableDataModel) {
    var userObjIdArr = [];
    userTableDataModel.forEach(function(userObjData){
        if (userObjData.remove) {
            userObjIdArr.push("#user"+userObjData.user.user_id);
        }
    });
    return userObjIdArr.join(",");
}

function createUserRowSelectorToCreate(userTableDataModel) {
    var userObjIdArr = [];
    userTableDataModel.forEach(function(userObjData){
        if (userObjData.create) {
            userObjIdArr.push("#user"+userObjData.user.user_id);
        }
    });
    return userObjIdArr.join(",");
}

function updateUserTable(jsonData) {
    var userTableDataModel = updateUserTableDataModel(jsonData.user_list);

    jQuery("#"+USER_TABLE).data(USER_DATA, userTableDataModel);

    updateStepHideAndRemoveRows(userTableDataModel);
}

function updateStepHideAndRemoveRows(userTableDataModel) {
    var userRowToRemoveSelector = createUserRowSelectorToRemove(userTableDataModel);
    if (userRowToRemoveSelector.length > 0) {
        jQuery(userRowToRemoveSelector).hide(500).promise().always(function() {
            jQuery(userRowToRemoveSelector).remove();
            updateStepCreateAndShowRows(userTableDataModel);
        });
    } else {
        updateStepCreateAndShowRows(userTableDataModel);
    }
}

function recreateUserTable(userTableDataModel) {
    //jQuery("#"+USER_TABLE+" tr").not(":first").remove();
    for (var i = 0; i < userTableDataModel.length; i++) {
        if (userTableDataModel[i].create) {
            addUserTableRow(userTableDataModel[i].user);
        }
    }
}

function updateStepCreateAndShowRows(userTableDataModel) {
    recreateUserTable(userTableDataModel);
    var userRowToCreateSelector = createUserRowSelectorToCreate(userTableDataModel);
    if (userRowToCreateSelector.length > 0) {
        jQuery(userRowToCreateSelector).show(500).promise().always(function() {
            updateStepBlinkRows(userTableDataModel);
        });
    } else {
        updateStepBlinkRows(userTableDataModel);
    }
}

function createUserStatusSelectorForEnter(userTableDataModel) {
    var userObjStatusIdArr = [];
    userTableDataModel.forEach(function(userObjData){
        if (userObjData.leaveEnterChange) {
            userObjStatusIdArr.push("#status"+userObjData.user.user_id);
        }
    });
    return userObjStatusIdArr.join(",");
}

function createUserStatusSelectorForLeave(userTableDataModel) {
    var userObjStatusIdArr = [];
    userTableDataModel.forEach(function(userObjData){
        if (userObjData.enterLeaveChange) {
            userObjStatusIdArr.push("#status"+userObjData.user.user_id);
        }
    });
    return userObjStatusIdArr.join(",");
}

function updateStepBlinkRows(userTableDataModel) {
    var userStatusLeftAreaSelector = createUserStatusSelectorForLeave(userTableDataModel);
    if (userStatusLeftAreaSelector.length > 0) {
        jQuery(userStatusLeftAreaSelector).html("LEFT AREA");
        jQuery(userStatusLeftAreaSelector).animate({
            backgroundColor: "#ff0000"
        }, 1000);
    }

    var userStatusEnteredAreaSelector = createUserStatusSelectorForEnter(userTableDataModel);
    if (userStatusEnteredAreaSelector.length > 0) {
        jQuery(userStatusEnteredAreaSelector).html("ENTERED AREA");
        jQuery(userStatusEnteredAreaSelector).animate({
            backgroundColor: "#00ff00"
        }, 1000);
    }
}

function handleCreateButton() {
    createUser();
}

function handleEnterButton(userObj) {
    enterArea(userObj.card_id);
}

function handleLeaveButton(userObj) {
    leaveArea(userObj.card_id);
}

function handleDeleteButton(userObj) {
    deleteUser(userObj.user_id);
}

function createActionCell(enterId, leaveId, deleteId) {
    return  "<div id=\"actionButtonsPanel\">" +
            "<button id=\""+enterId+"\">Enter</button>" +
            "<button id=\""+leaveId+"\">Leave</button>" +
            "<button id=\""+deleteId+"\">Delete</button>" +
            "</div>";
}

function decodeUserStatus(userObj) {
    if (userObj.entered_area == "true") {
        return "ENTERED AREA";
    }
    return "LEFT AREA";
}

function populateUserList() {
    jQuery.getJSON("/ela/user/",
        function (jsonData) {
            updateUserTable(jsonData);
        }
    );
}

function convertFormDataToJSON() {
    return JSON.stringify( { "user_id" : jQuery("input[name=user_id]").val(),
                              "card_id" : jQuery("input[name=card_id]").val(),
                              "entered_area" : "false" } );
}

function createUser() {
    jQuery.ajax({
        type     : "POST",
        cache    : false,
        url      : "/ela/user/",
        dataType : "json",
        contentType : "application/json",
        processData : false,
        data     : convertFormDataToJSON()
    }).done(function(response) {
        handleStatus(response)
        populateUserList();
    }).fail(function(jqXHR, textStatus) {
        alert("Unable to create user: " + textStatus);
    });
}

function handleStatus(response) {
    if (response.status_code == "FAIL") {
        var dialog = jQuery("<div></div>")
            .html(response.status_message)
            .dialog({
                modal: true,
                autoOpen: false,
                title: response.status_code,
                buttons: {
                    Ok: function() {
                        $(this).dialog("close");
                    }
                }
            });

        dialog.dialog("open");
    }
}

function deleteUser(userId) {
    jQuery.ajax({
        type     : "DELETE",
        cache    : false,
        url      : "/ela/user/"+userId,
        dataType : "json",
        contentType : "application/json",
        processData : false,
        data     : ""
    }).done(function(response) {
        handleStatus(response)
        populateUserList();
    }).fail(function(jqXHR, textStatus) {
        alert("Unable to delete user ["+userId+"]:" + textStatus);
    });
}

function enterArea(cardId) {
    jQuery.ajax({
        type     : "PUT",
        cache    : false,
        url      : "/ela/enter/"+cardId,
        dataType : "json",
        contentType : "application/json",
        processData : false,
        data     : JSON.stringify( { "entered_area" : "true" } )
    }).done(function(response) {
        handleStatus(response)
        populateUserList();
    }).fail(function(jqXHR, textStatus) {
        alert("Unable to enter area for card ["+cardId+"]:" + textStatus);
    });
}

function leaveArea(cardId) {
    jQuery.ajax({
        type     : "PUT",
        cache    : false,
        url      : "/ela/leave/"+cardId,
        dataType : "json",
        contentType : "application/json",
        processData : false,
        data     : JSON.stringify( { "entered_area" : "false" } )
    }).done(function(response) {
        handleStatus(response)
        populateUserList();
    }).fail(function(jqXHR, textStatus) {
        alert("Unable to leave area for card ["+cardId+"]:" + textStatus);
    });
}

function autoRefreshToggle() {
    var autoRefresh = jQuery("#autoRefreshButton").data("autoRefresh");
    if (typeof autoRefresh == "undefined" || autoRefresh == "false") {
        jQuery("#autoRefreshButton").button("option", "label", "Disable Auto Refresh").data("autoRefresh", "true");
        enableAutoRefresh();
    } else {
        jQuery("#autoRefreshButton").button("option", "label", "Enable Auto Refresh").data("autoRefresh", "false");
        disableAutoRefresh();
    }
}

function handleAutoRefreshOperation() {
    populateUserList()
}

function enableAutoRefresh() {
    var interval = setInterval(handleAutoRefreshOperation, 2000);
    jQuery("#autoRefreshButton").data("interval", interval);
}

function disableAutoRefresh() {
    var interval = jQuery("#autoRefreshButton").data("interval");
    clearInterval(interval);
}
