package ru.greenatom.demo.controller;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.greenatom.demo.domain.Document;
import ru.greenatom.demo.domain.User;
import ru.greenatom.demo.domain.dto.CreatedDocumentDto;
import ru.greenatom.demo.domain.dto.SavedDocumentDto;
import ru.greenatom.demo.repo.*;
import ru.greenatom.demo.service.DocumentService;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO
 **/
@RestController
@RequestMapping("/documents")
public class DocumentController {
    private final ModelMapper modelMapper;

    private final DocumentRepo documentRepo;
    private final DocumentService documentService;
    private final SecrecyLevelRepo secrecyLevelRepo;
    private final DocumentVersionRepo documentVersionRepo;
    private final DocumentHistoryRepo documentHistoryRepo;
    private final DocumentTypeRepo documentTypeRepo;

    Logger logger = LoggerFactory.getLogger(DocumentController.class);

    @Autowired
    public DocumentController(ModelMapper modelMapper, DocumentRepo documentRepo,
                              DocumentService documentService,
                              SecrecyLevelRepo secrecyLevelRepo,
                              DocumentVersionRepo documentVersionRepo,
                              DocumentHistoryRepo documentHistoryRepo, DocumentTypeRepo documentTypeRepo) {
        this.modelMapper = modelMapper;
        this.documentRepo = documentRepo;
        this.documentService = documentService;
        this.secrecyLevelRepo = secrecyLevelRepo;
        this.documentVersionRepo = documentVersionRepo;
        this.documentHistoryRepo = documentHistoryRepo;
        this.documentTypeRepo = documentTypeRepo;
    }

    /**
     * @param createdDocumentDto входной класс (Тело запроса)
     * @param bindingResult проверяет documentBuildingCreateModel на корректность(хранит в себе ошибки при сборке и собран ли он )
     **/
    @PostMapping
    @ResponseBody
    public Map<String, Object> create(
            @RequestBody @Valid CreatedDocumentDto createdDocumentDto,
            BindingResult bindingResult, Authentication user
    ) {
        Map<String, Object> strings = new HashMap<>();
        logger.info("documentBuildingCreateModel собрана:" + !bindingResult.hasErrors());
        if (bindingResult.hasErrors()) {
            List<String> errors = new ArrayList<String>();
            bindingResult.getAllErrors().forEach(e -> {
                logger.error("Error сборки documentBuildingCreateModel:" + e.getDefaultMessage());
                errors.add(e.getDefaultMessage());
            });
            strings.put("error", errors);
        } else {
            strings.put("id", this.documentService.create(createdDocumentDto,(User) user.getPrincipal()).getDocumentId());
        }
        return strings;
    }


    @PutMapping("/delete/{idDocument}")
    @ResponseBody
    public Document delete(@PathVariable String idDocument) {
        return documentService.delete(Long.parseLong(idDocument));

    }

    /**
     * @param buildingSaveModel входной класс (Тело запроса)
     * @param bindingResult проверяет documentBuildingCreateModel на корректность(хранит в себе
     *                      ошибки при сборке и собран ли он )
     **/
    @PutMapping("/{documentId}")
    @ResponseBody
    public Map<String, Object> save(
            @PathVariable String documentId,
            @RequestBody @Valid SavedDocumentDto buildingSaveModel,
            BindingResult bindingResult
    ) {
        Map<String, Object> strings = new HashMap<>();
        if (bindingResult.hasErrors()) {
            List<String> errors = new ArrayList<String>();

            bindingResult.getAllErrors().forEach(e -> {
                logger.error("Error сборки DocumentBuildingSaveModel:" + e.getDefaultMessage());
                errors.add(e.getDefaultMessage());
            });

            strings.put("error", errors);
        } else {
            Document document =this.documentService.save(buildingSaveModel);
            logger.info("DocumentBuildingSaveModel собрана:" + !bindingResult.hasErrors());
            strings.put("id", document.getDocumentId());
            strings.put("versionDocument",document.getDocumentId());
        }

        return strings;
    }
}
