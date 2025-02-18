package yps.systems.ai.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.*;
import yps.systems.ai.model.Role;
import yps.systems.ai.repository.IRoleRepository;

import java.util.Optional;

@RestController
@RequestMapping("/command/roleService")
public class RoleCommandController {

    private final IRoleRepository roleRepository;
    private final KafkaTemplate<String, Role> kafkaTemplate;

    @Value("${env.kafka.topicEvent}")
    private String kafkaTopicEvent;

    @Autowired
    public RoleCommandController(IRoleRepository roleRepository, KafkaTemplate<String, Role> kafkaTemplate) {
        this.roleRepository = roleRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping
    public ResponseEntity<String> saveRole(@RequestBody Role role) {
        Role roleSaved = roleRepository.save(role);
        Message<Role> message = MessageBuilder
                .withPayload(roleSaved)
                .setHeader(KafkaHeaders.TOPIC, kafkaTopicEvent)
                .setHeader("eventType", "CREATE_ROLE")
                .setHeader("source", "roleService")
                .build();
        kafkaTemplate.send(message);
        return new ResponseEntity<>("Role saved with ID: " + roleSaved.getElementId(), HttpStatus.CREATED);
    }

    @PostMapping("/{roleElementId}/{personElementId}")
    public ResponseEntity<String> saveRelationRole(@PathVariable String roleElementId, @PathVariable String personElementId) {
        roleRepository.setRoleTo(personElementId, roleElementId);
        return new ResponseEntity<>("Role saved with ID: " + roleElementId, HttpStatus.CREATED);
    }

    @DeleteMapping("/{elementId}")
    public ResponseEntity<String> deleteRole(@PathVariable String elementId) {
        Optional<Role> roleOptional = roleRepository.findById(elementId);
        if (roleOptional.isPresent()) {
            roleRepository.deleteRole(elementId);
            Message<String> message = MessageBuilder
                    .withPayload(elementId)
                    .setHeader(KafkaHeaders.TOPIC, kafkaTopicEvent)
                    .setHeader("eventType", "DELETE_ROLE")
                    .setHeader("source", "roleService")
                    .build();
            kafkaTemplate.send(message);
            return new ResponseEntity<>("Role deleted successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Role not founded", HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{roleElementId}/{personElementId}")
    public ResponseEntity<String> deleteRoleRelation(@PathVariable String roleElementId, @PathVariable String personElementId) {
        Optional<Role> roleOptional = roleRepository.findById(roleElementId);
        if (roleOptional.isPresent()) {
            roleRepository.deleteRoleRelation(personElementId);
            return new ResponseEntity<>("Role relation deleted successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Role relation not founded", HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{elementId}")
    public ResponseEntity<String> updateRole(@PathVariable String elementId, @RequestBody Role role) {
        Optional<Role> roleOptional = roleRepository.findById(elementId);
        if (roleOptional.isPresent()) {
            role.setElementId(roleOptional.get().getElementId());
            roleRepository.save(role);
            Message<Role> message = MessageBuilder
                    .withPayload(role)
                    .setHeader(KafkaHeaders.TOPIC, kafkaTopicEvent)
                    .setHeader("eventType", "UPDATE_ROLE")
                    .setHeader("source", "roleService")
                    .build();
            kafkaTemplate.send(message);
            return new ResponseEntity<>("Role updated successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Role not founded", HttpStatus.NOT_FOUND);
        }
    }

}
