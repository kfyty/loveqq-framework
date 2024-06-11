<mapper namespace="com.kfyty.database.mapper.UserMapper">
    <select id="findLikeName">
        <![CDATA[
        select * from user where 1
        <#if name?? && name?trim?length gt 0>
            and username like concat('%', ${r'#{name}'}, '%')
        </#if>
        ]]>
    </select>
</mapper>
