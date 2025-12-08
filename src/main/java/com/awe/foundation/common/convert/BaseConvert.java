package com.awe.foundation.common.convert;

import com.awe.foundation.manager.domain.area.dto.AreaReq;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * 基础转换器
 *
 * @author Awe
 * @since 2025/12/8 16:52
 */
public interface BaseConvert<E, AddReq, UpdateReq, Resp> {

    // Req -> Entity（查询）
    E toEntity(AreaReq req);

    // AddReq -> Entity（新增）
    E toEntity(AddReq req);

    // UpdateReq -> Entity（更新，覆盖目标对象）
    void updateEntity(UpdateReq req, @MappingTarget E entity);

    // Entity -> Resp（返回）
    Resp toResp(E entity);

    // List 转换
    List<Resp> toRespList(List<E> list);

    // 分页转换（MyBatis Plus）
    default IPage<Resp> toRespPage(IPage<E> page) {
        Page<Resp> respPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        respPage.setRecords(toRespList(page.getRecords()));
        return respPage;
    }
}
