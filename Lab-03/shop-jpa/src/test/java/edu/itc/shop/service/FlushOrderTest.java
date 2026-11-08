package edu.itc.shop.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import edu.itc.shop.domain.Tag;
import edu.itc.shop.repository.TagRepository;

@SpringBootTest
class FlushOrderTest {

    @Autowired TagRepository tags;

    
    @Test
    @Transactional  
    void deleteThenInsert_withoutFlushInBetween_violatesUniqueConstraint() {
        Tag saleTag = tags.saveAndFlush(new Tag("sale"));  
        tags.delete(saleTag);            
        tags.save(new Tag("sale"));      
        assertThatThrownBy(() -> tags.flush())
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @Transactional
    void deleteThenInsert_withFlushInBetween_works() {
        Tag saleTag = tags.saveAndFlush(new Tag("sale"));
        tags.delete(saleTag);
        tags.flush();                    
        Tag newSale = tags.save(new Tag("sale"));
        tags.flush();                    
        assertThat(newSale.getId()).isNotEqualTo(saleTag.getId());
        assertThat(tags.findAll())
                .filteredOn(t -> t.getName().equals("sale"))
                .hasSize(1);
    }
}