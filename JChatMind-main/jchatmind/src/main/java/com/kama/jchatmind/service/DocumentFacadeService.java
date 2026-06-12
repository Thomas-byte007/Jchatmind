package com.kama.jchatmind.service;

import com.kama.jchatmind.model.request.CreateDocumentRequest;
import com.kama.jchatmind.model.request.UpdateDocumentRequest;
import com.kama.jchatmind.model.response.CreateDocumentResponse;
import com.kama.jchatmind.model.response.GetDocumentsResponse;
import org.springframework.web.multipart.MultipartFile;

/*
 * ???????? -- ?????????,??????
 * createDocument():?????;uploadDocument():????+????+???
 * MultipartFile:Spring???????,???FormData?
 */
public interface DocumentFacadeService {
    GetDocumentsResponse getDocuments();
    GetDocumentsResponse getDocumentsByKbId(String kbId);  // ??????
    CreateDocumentResponse createDocument(CreateDocumentRequest r);
    CreateDocumentResponse uploadDocument(String kbId, MultipartFile file);
    void deleteDocument(String documentId);
    void updateDocument(String documentId, UpdateDocumentRequest r);
}