package com.kama.jchatmind.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kama.jchatmind.converter.KnowledgeBaseConverter;
import com.kama.jchatmind.exception.BizException;
import com.kama.jchatmind.mapper.KnowledgeBaseMapper;
import com.kama.jchatmind.model.dto.KnowledgeBaseDTO;
import com.kama.jchatmind.model.entity.KnowledgeBase;
import com.kama.jchatmind.model.request.CreateKnowledgeBaseRequest;
import com.kama.jchatmind.model.request.UpdateKnowledgeBaseRequest;
import com.kama.jchatmind.model.response.CreateKnowledgeBaseResponse;
import com.kama.jchatmind.model.response.GetKnowledgeBasesResponse;
import com.kama.jchatmind.model.vo.KnowledgeBaseVO;
import com.kama.jchatmind.service.KnowledgeBaseFacadeService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/*
 * ??????? -- ??????????
 * ??? = AI?"???",????Document,??RAG(??????)
 * ????????????(?DocumentMapper????)
 */
@Service
@AllArgsConstructor
public class KnowledgeBaseFacadeServiceImpl implements KnowledgeBaseFacadeService {

    private final KnowledgeBaseMapper knowledgeBaseMapper;
    private final KnowledgeBaseConverter knowledgeBaseConverter;

    @Override
    public GetKnowledgeBasesResponse getKnowledgeBases() {
        List<KnowledgeBase> kbs = knowledgeBaseMapper.selectAll();
        List<KnowledgeBaseVO> result = new ArrayList<>();
        for (KnowledgeBase kb : kbs) {
            try { result.add(knowledgeBaseConverter.toVO(kb)); }
            catch (JsonProcessingException e) { throw new RuntimeException(e); }
        }
        return GetKnowledgeBasesResponse.builder().knowledgeBases(result.toArray(new KnowledgeBaseVO[0])).build();
    }

    @Override
    public CreateKnowledgeBaseResponse createKnowledgeBase(CreateKnowledgeBaseRequest request) {
        try {
            KnowledgeBaseDTO dto = knowledgeBaseConverter.toDTO(request);
            KnowledgeBase kb = knowledgeBaseConverter.toEntity(dto);
            LocalDateTime now = LocalDateTime.now();
            kb.setCreatedAt(now);
            kb.setUpdatedAt(now);
            if (knowledgeBaseMapper.insert(kb) <= 0) throw new BizException("???????");
            return CreateKnowledgeBaseResponse.builder().knowledgeBaseId(kb.getId()).build();
        } catch (JsonProcessingException e) { throw new BizException("?????: " + e.getMessage()); }
    }

    @Override
    public void deleteKnowledgeBase(String kbId) {
        KnowledgeBase kb = knowledgeBaseMapper.selectById(kbId);
        if (kb == null) throw new BizException("??????: " + kbId);
        if (knowledgeBaseMapper.deleteById(kbId) <= 0) throw new BizException("???????");
    }

    @Override
    public void updateKnowledgeBase(String kbId, UpdateKnowledgeBaseRequest request) {
        try {
            KnowledgeBase existing = knowledgeBaseMapper.selectById(kbId);
            if (existing == null) throw new BizException("??????: " + kbId);
            KnowledgeBaseDTO dto = knowledgeBaseConverter.toDTO(existing);
            knowledgeBaseConverter.updateDTOFromRequest(dto, request);
            KnowledgeBase updated = knowledgeBaseConverter.toEntity(dto);
            updated.setId(existing.getId());
            updated.setCreatedAt(existing.getCreatedAt());
            updated.setUpdatedAt(LocalDateTime.now());
            if (knowledgeBaseMapper.updateById(updated) <= 0) throw new BizException("???????");
        } catch (JsonProcessingException e) { throw new BizException("?????: " + e.getMessage()); }
    }
}