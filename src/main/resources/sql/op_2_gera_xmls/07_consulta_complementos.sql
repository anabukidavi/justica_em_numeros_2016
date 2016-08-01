select 
    tc.cd_tipo_complemento, 
    tc.ds_nome,
    cs.ds_texto AS cd_complemento,
    regexp_replace(cs.ds_valor_complemento, '[\r\n]', '') AS nm_complemento
from tb_complemento_segmentado cs
inner join client.tb_tipo_complemento tc ON (tc.id_tipo_complemento = cs.id_tipo_complemento)
where 1=1
    and cs.id_movimento_processo = :id_processo_evento
    and tc.cd_tipo_complemento is not null
    and tc.ds_nome is not null
    and
    (0=1
      or '' <> trim(cs.ds_texto) 
      or cs.ds_valor_complemento is not null
    )