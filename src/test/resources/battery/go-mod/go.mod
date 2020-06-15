module github.com/sagikazarmark/modern-go-application

        go 1.14

        replace contrib.go.opencensus.io/integrations/ocsql v0.1.5 => contrib.go.opencensus.io/integrations/ocsql v0.1.6

        require (
        contrib.go.opencensus.io/exporter/ocagent v0.6.0
        contrib.go.opencensus.io/exporter/prometheus v0.1.0
        contrib.go.opencensus.io/integrations/ocsql v0.1.5
        emperror.dev/emperror v0.32.0
        emperror.dev/errors v0.7.0
        emperror.dev/handler/logur v0.4.0
        github.com/99designs/gqlgen v0.11.3
        github.com/AppsFlyer/go-sundheit v0.2.0
        github.com/ThreeDotsLabs/watermill v1.1.1
        github.com/cloudflare/tableflip v1.2.0
        github.com/facebookincubator/ent v0.2.1
        github.com/go-kit/kit v0.10.0
        github.com/go-sql-driver/mysql v1.5.1-0.20200311113236-681ffa848bae
        github.com/golang/protobuf v1.4.2
        github.com/goph/idgen v0.4.0
        github.com/gorilla/mux v1.7.4
        github.com/markbates/pkger v0.15.1
        github.com/mccutchen/go-httpbin v0.0.0-20190116014521-c5cb2f4802fa
        github.com/oklog/run v1.1.0
        github.com/olekukonko/tablewriter v0.0.4
        github.com/sagikazarmark/appkit v0.9.0
        github.com/sagikazarmark/kitx v0.12.0
        github.com/sagikazarmark/ocmux v0.2.0
        github.com/sirupsen/logrus v1.6.0
        github.com/spf13/cobra v1.0.0
        github.com/spf13/pflag v1.0.5
        github.com/spf13/viper v1.7.0
        github.com/stretchr/testify v1.5.1
        github.com/vektah/gqlparser/v2 v2.0.1
        go.opencensus.io v0.22.3
        golang.org/x/xerrors v0.0.0-20191204190536-9bdfabe68543
        google.golang.org/genproto v0.0.0-20200515170657-fc4c6c6a6587
        google.golang.org/grpc v1.29.1
        google.golang.org/protobuf v1.23.0
        logur.dev/adapter/logrus v0.5.0
        logur.dev/integration/watermill v0.5.0
        logur.dev/logur v0.16.2
        )
