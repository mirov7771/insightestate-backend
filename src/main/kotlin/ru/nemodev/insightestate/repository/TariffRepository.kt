package ru.nemodev.insightestate.repository

import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import ru.nemodev.insightestate.entity.ReportEntity
import ru.nemodev.insightestate.entity.TariffEntity
import java.util.*

@Repository
interface TariffRepository: ListCrudRepository<TariffEntity, UUID> {
    fun findByTitle(name: String): TariffEntity?

    @Query("""
select u.user_detail ->> 'fio' as fio,
	   u.user_detail ->> 'login' as login,
	   u.user_detail ->> 'mobileNumber' as mobile,
	   u.updated_at as last_date,
	   u.user_detail ->> 'group' as group_name,
	   (select count(*) from estate_collections ec where (ec.collection_detail ->> 'userId')::uuid = u.id) as collections,
	   (
	   	select max(t.title)
	   	   from subscription s
		inner join tariff t 
		on t.id = s.main_id 
	   	  where s.user_id = u.id 
	   	  group by s.user_id 
	   ) as tariff
  from users u
 order by 6 desc, 4 desc 
    """)
    fun getReport(): List<ReportEntity>
}
