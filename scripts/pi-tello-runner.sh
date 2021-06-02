export RYZE_TELLO_COORDINATOR_SERVER_HOST=40.115.62.3
export RYZE_TELLO_COORDINATOR_SERVER_PORT=50000
export RYZE_TELLO_COORDINATOR_SERVER_VIDEO_PORT=50010
export RYZE_TELLO_DRONE_HOST=192.168.10.1
export RYZE_TELLO_DRONE_PORT=8889
export RYZE_TELLO_DRONE_VIDEO_PORT=11111

while true; do

  # Try to connect to drone network
  while true; do
      ping -c1 -q 192.168.10.1
      droneNetworkStatus=$?;

      if [ $droneNetworkStatus -eq 0 ]; then
          break
      else
          # Backup link for development purposes
          ping -c1 -q 192.168.100.1
          homeNetworkStatus=$?;

          if [ $homeNetworkStatus -eq 0 ]; then
              break
          fi

          # not connected, sleeping for a second
          sleep 1
      fi

  done

  # Try to connect to internet and coordinator server via 4G
  sakis3g connect

  while true; do
      ping -c1 -q 40.115.62.3
      internetConnectionStatus=$?;

      if [ $internetConnectionStatus -eq 0 ]; then
          break
      else
          # not connected, sleeping for a second
          sleep 1
      fi
  done

  # Connected to both internet and drone AP, run communicator
  cd /home/pi/ryze-tello/
  java my.project.fer.ryzetello.client.RyzeTelloRaspberryPiTcpClient

  sakis3g disconnect
done
