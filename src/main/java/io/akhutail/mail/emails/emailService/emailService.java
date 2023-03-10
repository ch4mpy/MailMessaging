package io.akhutail.mail.emails.emailService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.datastax.oss.driver.api.core.uuid.Uuids;

import io.akhutail.mail.emails.emailsById.EmailsById;
import io.akhutail.mail.emails.emailsById.EmailsRepo;
import io.akhutail.mail.emails.emailsByUserFolder.EmailsByUserFolder;
import io.akhutail.mail.emails.emailsByUserFolder.EmailsByUserFolderRepo;


@Service
public class emailService {
    @Autowired private EmailsRepo emailsRepo;

    @Autowired private EmailsByUserFolderRepo emailsByUserFolderRepo;
    

    public UUID sendEmail(EmailsById email) {

        UUID timeUuid = Uuids.timeBased();
        List<String> toUserIdList = email.getTo().stream()
                                    .map(id -> StringUtils.trimWhitespace(id))
                                    .filter(id -> StringUtils.hasText(id)).collect(Collectors.toList());
       
        String sender = "sender123";
        EmailsByUserFolder sentItemEntry = new EmailsByUserFolder(sender, "Sent", timeUuid, sender,
                                                                  email.getSubject(), true);

        // adding to Sent Folder of sender
        
        emailsByUserFolderRepo.save(sentItemEntry);
        
        
        EmailsByUserFolder inboxEntry = sentItemEntry;
        inboxEntry.setIsRead(false);
        inboxEntry.setLabel("Inbox");

        // Adding to inbox of each reciever
        toUserIdList.forEach(toUserId -> {
            inboxEntry.setUserId(toUserId);
            emailsByUserFolderRepo.save(inboxEntry);
        });

        // Save email
        email.setId(timeUuid);
        email.setFrom("akhutail");
        emailsRepo.save(email);

        return timeUuid;
    }
}
