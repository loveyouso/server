/*
 * This file is part of the Wildfire Chat package.
 * (c) Heavyrain2012 <heavyrain.lee@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.moquette.imhandler;

import cn.wildfirechat.proto.WFCMessage;
import cn.wildfirechat.pojos.GroupNotificationBinaryContent;
import io.moquette.spi.impl.Qos1PublishHandler;
import io.netty.buffer.ByteBuf;
import cn.wildfirechat.common.ErrorCode;

import java.util.Set;

import static win.liyufan.im.IMTopic.QuitGroupTopic;

@Handler(value = QuitGroupTopic)
public class QuitGroupHandler extends GroupHandler<WFCMessage.QuitGroupRequest> {
    @Override
    public ErrorCode action(ByteBuf ackPayload, String clientID, String fromUser, boolean isAdmin, WFCMessage.QuitGroupRequest request, Qos1PublishHandler.IMCallback callback) {
        if(request.hasNotifyContent() && request.getNotifyContent().getType() > 0 && !isAdmin && !m_messagesStore.isAllowClientCustomGroupNotification()) {
            return ErrorCode.ERROR_CODE_NOT_RIGHT;
        }

        ErrorCode errorCode = m_messagesStore.quitGroup(fromUser, request.getGroupId());
        if (errorCode == ErrorCode.ERROR_CODE_SUCCESS) {
            if (request.hasNotifyContent() && request.getNotifyContent().getType() > 0 && (isAdmin || m_messagesStore.isAllowClientCustomGroupNotification())) {
                sendGroupNotification(fromUser, request.getGroupId(), request.getToLineList(), request.getNotifyContent());
            } else {
                WFCMessage.MessageContent content = new GroupNotificationBinaryContent(request.getGroupId(), fromUser, null, "").getQuitGroupNotifyContent();
                sendGroupNotification(fromUser, request.getGroupId(), request.getToLineList(), content);
            }

            if((m_messagesStore.getVisibleQuitKickoffNotification() & 0x01) > 0) {
                Set<String> toUsers = m_messagesStore.getGroupManagers(request.getGroupId(), true);
                WFCMessage.MessageContent content = new GroupNotificationBinaryContent(request.getGroupId(), fromUser, null, "").getQuitVisibleGroupNotifyContent();
                sendGroupNotification(fromUser, request.getGroupId(), request.getToLineList(), content, toUsers);
            }
        }
        return errorCode;
    }
}
