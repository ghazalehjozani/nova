package ir.dotin.loan.trade.adapters.driven.persistence.shared.query;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import ir.dotin.platform.adapter.persistence.entity.PersistentEntity;
import ir.dotin.platform.adapter.persistence.repository.PersistentRepository;
import ir.dotin.loan.trade.core.application.query.shared.exception.InvalidCursorException;
import ir.dotin.loan.trade.core.application.query.shared.pagination.CursorEncoder;
import ir.dotin.loan.trade.core.application.query.shared.pagination.CursorPage;
import ir.dotin.loan.trade.core.application.query.shared.pagination.CursorPageRequest;
import ir.dotin.loan.trade.core.application.query.shared.pagination.CursorPosition;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@RequiredArgsConstructor
public abstract class AbstractCursorPagingAdapter<EntityType extends PersistentEntity, QueryDtoType> {

    protected final CursorEncoder cursorEncoder;

    protected ScrollPosition buildScrollPosition(CursorPageRequest pageRequest) {
        if (pageRequest.cursor() == null || pageRequest.cursor().isBlank()) {
            return ScrollPosition.keyset();
        }

        try {
            cursorEncoder.validate(pageRequest.cursor());
            CursorPosition position = cursorEncoder.decode(pageRequest.cursor());

            return ScrollPosition.forward(Map.of(
                    "createdAt", position.timestamp(),
                    "id", position.id()));
        } catch (Exception e) {
            throw new InvalidCursorException("Failed to decode cursor position", e);
        }
    }

    protected CursorPage<QueryDtoType> buildCursorPage(
            Window<EntityType> window, CursorPageRequest pageRequest, Function<EntityType, QueryDtoType> mapper) {

        List<QueryDtoType> items = window.getContent().stream().map(mapper).toList();

        String nextCursor = null;
        String previousCursor = pageRequest.cursor();

        if (window.hasNext() && !items.isEmpty()) {
            QueryDtoType lastItem = items.getLast();
            CursorPosition position = createCursorPosition(lastItem);
            nextCursor = cursorEncoder.encode(position);
        }

        return new CursorPage<>(items, nextCursor, previousCursor, window.hasNext(), !pageRequest.isFirstPage());
    }

    protected abstract CursorPosition createCursorPosition(QueryDtoType queryDto);

    protected abstract Window<EntityType> findAllWithCursor(
            CursorPageRequest pageRequest, PersistentRepository<EntityType> repository);

    protected CursorPage<QueryDtoType> buildCursorPageFromWindow(
            Window<EntityType> window, CursorPageRequest pageRequest, Function<EntityType, QueryDtoType> mapper) {

        return buildCursorPage(window, pageRequest, mapper);
    }

    protected Optional<QueryDtoType> findById(
            UUID id, JpaRepository<EntityType, UUID> repository, Function<EntityType, QueryDtoType> mapper) {

        return repository.findById(id).map(mapper);
    }
}
