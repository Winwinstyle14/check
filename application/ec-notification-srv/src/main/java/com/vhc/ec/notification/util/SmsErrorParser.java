package com.vhc.ec.notification.util;

/**
 * Created by gnu on 11/18/15.
 */
public class SmsErrorParser {

    public static String GetErrStringByCommandStatus(int status) {
        switch (status) {
            case -1:
                return "Connect to SMPP Host failed or Bind failed (login/bind failed – invalid login credentials or login restricted by IP address)";
            case 0:
                return "Success";
            case 1:
                return "Invalid Message Length (sm_length parameter)";
            case 2:
                return "Invalid Command Length (command_length in SMPP PDU)";
            case 3:
                return "Invalid Command ID (command_id in SMPP PDU)";
            case 4:
                return "Incorrect BIND status for given command (example: trying to submit a message when bound only as a receiver)";
            case 5:
                return "ESME already in bound state (example: sending a second bind command during an existing SMPP session)";
            case 6:
                return "Invalid Priority Flag (priority_flag parameter)";
            case 7:
                return "Invalid Regstered Delivery Flag (registered_delivery parameter)";
            case 8:
                return "System Error (indicates server problems on the SMPP host)";
            case 0x0A:
                return "Invalid source address (sender/source address is not valid)";
            case 0x0B:
                return "Invalid desintation address (recipient/destination phone number is not valid)";
            case 0x0C:
                return "Message ID is invalid (error only relevant to query_sm, replace_sm, cancel_sm commands)";
            case 0x0D:
                return "Bind failed (login/bind failed – invalid login credentials or login restricted by IP address)";
            case 0x0E:
                return "Invalid password (login/bind failed)";
            case 0x0F:
                return "Invalid System ID (login/bind failed – invalid username / system id)";
            case 0x11:
                return "cancel_sm request failed";
            case 0x13:
                return "replace_sm request failed";
            case 0x14:
                return "Message Queue Full (This can indicate that the SMPP server has too many queued messages and temporarily cannot accept any more messages. It can also indicate that the SMPP server has too many messages pending for the specified recipient and will not accept any more messages for this recipient until it is able to deliver messages that are already in the queue to this recipient.)";
            case 0x15:
                return "Invalid service_type value";
            case 0x33:
                return "Invalid number_of_dests value in submit_multi request";
            case 0x34:
                return "Invalid distribution list name in submit_multi request";
            case 0x40:
                return "Invalid dest_flag in submit_multi request";
            case 0x42:
                return "Invalid ‘submit with replace’ request (replace_if_present flag set)";
            case 0x43:
                return "Invalid esm_class field data";
            case 0x44:
                return "Cannot submit to distribution list (submit_multi request)";
            case 0x45:
                return "Submit message failed";
            case 0x48:
                return "Invalid Source address TON";
            case 0x49:
                return "Invalid Source address NPI";
            case 0x50:
                return "Invalid Destination address TON";
            case 0x51:
                return "Invalid Destination address NPI";
            case 0x53:
                return "Invalid system_type field";
            case 0x54:
                return "Invalid replace_if_present flag";
            case 0x55:
                return "Invalid number_of_messages parameter";
            case 0x58:
                return "Throttling error (This indicates that you are submitting messages at a rate that is faster than the provider allows)";
            case 0x61:
                return "Invalid schedule_delivery_time parameter";
            case 0x62:
                return "Invalid validity_period parameter / Expiry time";
            case 0x63:
                return "Invalid sm_default_msg_id parameter (this error can sometimes occur if the 'Default Sender Address' field is blank in NowSMS)";
            case 0x64:
                return "ESME Receiver Temporary App Error Code";
            case 0x65:
                return "ESME Receiver Permanent App Error Code (the SMPP provider is rejecting the message due to a policy decision or message filter)";
            case 0x66:
                return "ESME Receiver Reject Message Error Code (the SMPP provider is rejecting the message due to a policy decision or message filter)";
            case 0x67:
                return "query_sm request failed";
            case 0xC0:
                return "Error in the optional TLV parameter encoding";
            case 0xC1:
                return "An optional TLV parameter was specified which is not allowed";
            case 0xC2:
                return "An optional TLV parameter has an invalid parameter length";
            case 0xC3:
                return "An expected optional TLV parameter is missing";
            case 0xC4:
                return "An optional TLV parameter is encoded with an invalid value";
            case 0xFE:
                return "Generice Message Delivery failure";
            case 0xFF:
                return "An unknown error occurred (indicates server problems on the SMPP host)";
            default:
                return "Unknown";
        }
    }
}
