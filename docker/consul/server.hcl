# Consul server (single-node dev, ACLs on — mirrors production posture)
datacenter  = "dc1"
data_dir    = "/consul/data"
server      = true
bootstrap_expect = 1
node_name   = "consul-server-1"
client_addr = "0.0.0.0"
bind_addr   = "{{ GetInterfaceIP \"eth0\" }}"
log_level   = "WARN"
disable_update_check = true   # no outbound calls to checkpoint-api.hashicorp.com

ui_config {
  enabled = true
}

connect {
  enabled = true
}

acl {
  enabled                  = true
  default_policy           = "deny"
  enable_token_persistence = true
  down_policy              = "extend-cache"
}

telemetry {
  prometheus_retention_time = "30s"
  disable_hostname          = true
}
