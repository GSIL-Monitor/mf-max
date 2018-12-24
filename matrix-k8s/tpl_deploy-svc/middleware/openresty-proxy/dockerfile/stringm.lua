local _M = { _VERSION = '0.01' }
function _M.split(s, p)
    local rt= {}
    string.gsub(s, '[^'..p..']+', function(w) table.insert(rt, w) end )
    return rt
end
return _M
