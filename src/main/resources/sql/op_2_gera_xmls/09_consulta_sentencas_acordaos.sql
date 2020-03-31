SELECT dt_juntada, ds_login
FROM tb_processo_documento doc
INNER JOIN tb_proc_doc_bin_pess_assin assin USING (id_processo_documento_bin)
INNER JOIN tb_usuario_login ul ON (ul.id_usuario = assin.id_pessoa)
WHERE doc.id_processo = :id_processo
  AND doc.in_ativo = 'S'
  AND doc.dt_juntada IS NOT NULL
  AND doc.id_tipo_processo_documento::varchar IN (
	SELECT vl_variavel
	FROM tb_parametro
	WHERE nm_variavel IN ('idTipoProcessoDocumentoAcordao', 'idTipoProcessoDocumentoSentenca')
  )
ORDER BY dt_juntada DESC